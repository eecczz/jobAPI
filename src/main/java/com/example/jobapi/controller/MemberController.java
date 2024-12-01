package com.example.jobapi.controller;

import com.example.jobapi.dto.LoginRequest;
import com.example.jobapi.dto.LoginResponse;
import com.example.jobapi.entity.Member;
import com.example.jobapi.repository.MemberRepository;
import com.example.jobapi.util.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/member")
public class MemberController {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody Member member) {
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberRepository.save(member);
        return ResponseEntity.ok("회원가입 성공");
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        Member member = memberRepository.findByUsername(loginRequest.getUsername());
        if (member == null || !passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())) {
            return ResponseEntity.status(401).body(null); // 인증 실패
        }

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(member.getUsername());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    // 사용자 정보 가져오기 (JWT 기반)
    @GetMapping("/me")
    public ResponseEntity<Member> getCurrentUser(@RequestHeader("Authorization") String token) {
        String username = jwtUtil.extractUsername(token.substring(7)); // "Bearer " 제거
        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            return ResponseEntity.status(404).body(null);
        }
        return ResponseEntity.ok(member);
    }
}
