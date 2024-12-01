package com.example.jobapi.controller;

import com.example.jobapi.dto.AuthResponse;
import com.example.jobapi.dto.LoginRequest;
import com.example.jobapi.dto.SignupRequest;
import com.example.jobapi.entity.Member;
import com.example.jobapi.repository.MemberRepository;
import com.example.jobapi.util.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;

    @GetMapping("/login")
    public String loginPage() {
        return "signin"; // login.html을 반환
    }

    // 회원가입 페이지 요청
    @GetMapping("/signup")
    public String signupPage() {
        return "signup"; // signup.html을 반환
    }

    public AuthController(JWTUtil jwtUtil, PasswordEncoder passwordEncoder, MemberRepository memberRepository) {
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.memberRepository = memberRepository;
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest, HttpSession session) {
        Member member = memberRepository.findByUsername(loginRequest.getUsername());
        if (member != null && passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            String token = jwtUtil.generateToken(member.getUsername());

            // 세션에 사용자 정보 저장
            session.setAttribute("username", member.getUsername());
            session.setAttribute("loggedIn", true);

            return ResponseEntity.ok(new AuthResponse(token));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션 무효화
        session.invalidate();
        return "redirect:/auth/login";
    }




    @PostMapping("/signup")
    @ResponseBody
    public ResponseEntity<?> signup(@RequestBody SignupRequest signupRequest) {
        // 1. 중복된 사용자명 검증
        if (memberRepository.findByUsername(signupRequest.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }

        // 2. Member 객체 생성 및 값 설정
        Member member = new Member();
        member.setUsername(signupRequest.getUsername()); // 사용자명 설정
        member.setPassword(passwordEncoder.encode(signupRequest.getPassword())); // 비밀번호 암호화

        // 3. 데이터베이스에 사용자 저장
        memberRepository.save(member);

        // 4. 성공 메시지 반환
        return ResponseEntity.ok("Signup successful");
    }

}
