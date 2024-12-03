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
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Controller
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

    @PostMapping("/register")
    @ResponseBody
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
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
        member.setUsername(username); // 사용자명 설정
        member.setPassword(passwordEncoder.encode(signupRequest.getPassword())); // 비밀번호 암호화
        memberRepository.save(member);

        return ResponseEntity.ok("Signup successful");
    }

    // 회원 이력 페이지
    @GetMapping("/profile")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> memberHistory(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }

        // 반환할 데이터를 Map에 담음
        Map<String, Object> response = new HashMap<>();
        response.put("member", member);
        response.put("saveList", saveListRepository.findByMember(member));
        response.put("appList", appListRepository.findByMember(member));

        return ResponseEntity.ok(response); // JSON 형식으로 응답 반환
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> updates, HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }

        // 이메일 변경
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

        // 비밀번호 변경
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

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Member member = memberRepository.findByUsername(loginRequest.getUsername());
        if (member == null) {
            throw new NotFoundException("사용자를 찾을 수 없습니다.");
        }

        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            throw new UnauthorizedException("잘못된 인증 정보입니다.");
        }

        String token = jwtUtil.generateToken(member.getUsername());

        // 세션에 사용자 정보 저장
        session.setAttribute("username", member.getUsername());
        session.setAttribute("loggedIn", true);

        return ResponseEntity.ok()
                .body(Map.of(
                        "message", "Login successful",
                        "redirectUrl", "/demo/list",
                        "token", token
                ));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshAccessToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken().trim(); // 토큰 값 추출
        try {
            String username = jwtUtil.extractUsername(refreshToken);

            if (username == null || !jwtUtil.validateToken(refreshToken, username)) {
                throw new UnauthorizedException("잘못된 리프레시 토큰입니다.");
            }

            Member member = memberRepository.findByUsername(username);
            if (member == null) {
                throw new NotFoundException("사용자를 찾을 수 없습니다.");
            }

            // 새로운 액세스 토큰 생성 (필요시)
            // String newAccessToken = jwtUtil.generateToken(username);

            return ResponseEntity.ok("Token refreshed successfully");
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnauthorizedException("토큰 갱신에 실패했습니다.");
        }
    }

}
