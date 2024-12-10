package com.example.jobapi.controller;

import com.example.jobapi.dto.AuthResponse;
import com.example.jobapi.dto.LoginRequest;
import com.example.jobapi.dto.RefreshTokenRequest;
import com.example.jobapi.dto.SignupRequest;
import com.example.jobapi.entity.Member;
import com.example.jobapi.exception.ForbiddenException;
import com.example.jobapi.exception.NotFoundException;
import com.example.jobapi.exception.UnauthorizedException;
import com.example.jobapi.repository.AppListRepository;
import com.example.jobapi.repository.MemberRepository;
import com.example.jobapi.repository.SaveListRepository;
import com.example.jobapi.util.JWTUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    @Autowired
    private SaveListRepository saveListRepository;
    @Autowired
    private AppListRepository appListRepository;

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";

    public AuthController(JWTUtil jwtUtil, PasswordEncoder passwordEncoder, MemberRepository memberRepository) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
    }

    @Operation(summary = "회원가입", description = "새로운 사용자 계정을 생성합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 이메일 형식 또는 중복된 사용자 이름")
    })
    @PostMapping("/register")
    public ResponseEntity<?> signup(
            @Parameter(description = "회원가입 요청 데이터", required = true)
            @RequestBody SignupRequest signupRequest) {
        String username = signupRequest.getUsername();

        // 이메일 형식 검증
        if (!Pattern.matches(EMAIL_REGEX, username)) {
            throw new ForbiddenException("이메일 형식이 올바르지 않습니다.");
        }

        // 중복된 사용자명 검증
        if (memberRepository.findByUsername(username) != null) {
            throw new ForbiddenException("이미 사용 중인 사용자 이름입니다.");
        }

        // 회원가입 처리
        Member member = new Member();
        member.setUsername(username);
        member.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        memberRepository.save(member);

        return ResponseEntity.ok("Signup successful");
    }

    @Operation(summary = "사용자 프로필 조회", description = "사용자의 세부 정보, 지원 내역, 관심 공고 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 정보 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다.")
    })
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> memberHistory(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("member", member);
        response.put("saveList", saveListRepository.findByMember(member));
        response.put("appList", appListRepository.findByMember(member));

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "사용자 프로필 수정", description = "사용자의 이메일 또는 비밀번호를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "프로필 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 이메일 형식 또는 비밀번호가 비어 있음"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다.")
    })
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(
            @Parameter(description = "프로필 수정 요청 데이터", required = true)
            @RequestBody Map<String, String> updates, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }

        if (updates.containsKey("email")) {
            String newEmail = updates.get("email");
            if (!Pattern.matches(EMAIL_REGEX, newEmail)) {
                throw new ForbiddenException("이메일 형식이 올바르지 않습니다.");
            }
            if (!newEmail.equals(member.getUsername()) && memberRepository.findByUsername(newEmail) != null) {
                throw new ForbiddenException("이미 사용 중인 이메일입니다.");
            }
            member.setUsername(newEmail);
        }

        if (updates.containsKey("password")) {
            String newPassword = updates.get("password");
            if (newPassword.isEmpty()) {
                throw new ForbiddenException("비밀번호는 비워둘 수 없습니다.");
            }
            member.setPassword(passwordEncoder.encode(newPassword));
        }

        memberRepository.save(member);

        return ResponseEntity.ok(Map.of(
                "message", "회원정보가 성공적으로 수정되었습니다.",
                "username", member.getUsername()
        ));
    }

    @Operation(summary = "로그인", description = "사용자가 로그인하고 JWT 토큰을 반환받습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "401", description = "잘못된 사용자 이름 또는 비밀번호")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Parameter(description = "로그인 요청 데이터", required = true)
            @RequestBody LoginRequest loginRequest, HttpSession session) {
        Member member = memberRepository.findByUsername(loginRequest.getUsername());
        if (member == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new UnauthorizedException("잘못된 인증 정보입니다.");
        }

        String token = jwtUtil.generateToken(member.getUsername());

        session.setAttribute("username", member.getUsername());
        session.setAttribute("loggedIn", true);

        return ResponseEntity.ok(Map.of(
                "message", "Login successful",
                "redirectUrl", "/demo/list",
                "token", token
        ));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh 토큰을 검증하고 새로운 Access 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "잘못된 리프레시 토큰")
    })
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(
            @Parameter(description = "Refresh 토큰 요청 데이터", required = true)
            @RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken().trim();
        try {
            String username = jwtUtil.extractUsername(refreshToken);

            if (username == null || !jwtUtil.validateToken(refreshToken, username)) {
                throw new UnauthorizedException("잘못된 리프레시 토큰입니다.");
            }

            Member member = memberRepository.findByUsername(username);
            if (member == null) {
                throw new NotFoundException("사용자를 찾을 수 없습니다.");
            }

            return ResponseEntity.ok("Token refreshed successfully");
        } catch (Exception e) {
            throw new UnauthorizedException("토큰 갱신에 실패했습니다.");
        }
    }
}
