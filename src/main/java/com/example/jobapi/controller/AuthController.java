package com.example.jobapi.controller;

import com.example.jobapi.dto.AuthResponse;
import com.example.jobapi.dto.LoginRequest;
import com.example.jobapi.dto.SignupRequest;
import com.example.jobapi.entity.Member;
import com.example.jobapi.repository.AppListRepository;
import com.example.jobapi.repository.MemberRepository;
import com.example.jobapi.repository.SaveListRepository;
import com.example.jobapi.util.JWTUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/demo")
public class AuthController {

    private final JWTUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    @Autowired
    private SaveListRepository saveListRepository;
    @Autowired
    private AppListRepository appListRepository;

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

            // 로그인 성공 시 리다이렉트 URL 반환
            return ResponseEntity.ok()
                    .body(Map.of(
                            "message", "Login successful",
                            "redirectUrl", "/demo/list",
                            "token", token
                    ));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }


    @GetMapping("/logout")
    public String logout(HttpSession session) {
        // 세션 무효화
        session.invalidate();
        return "redirect:/demo/login";
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

    // 회원 이력 페이지
    @GetMapping("/member-history")
    public String memberHistory(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/demo/login";
        }

        Member member = memberRepository.findByUsername(username);
        model.addAttribute("member", member);
        model.addAttribute("saveList", saveListRepository.findByMember(member));
        model.addAttribute("appList", appListRepository.findByMember(member));
        return "member-history";
    }

    // 회원 정보 수정 페이지
    @GetMapping("/member-modify")
    public String modifyMemberForm(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/demo/login";
        }

        Member member = memberRepository.findByUsername(username);
        model.addAttribute("member", member);
        return "member-modify";
    }

    // 회원 정보 수정 처리
    @PostMapping("/member-modify")
    public String modifyMember(@RequestParam("password") String password,
                               @RequestParam("passwordConfirm") String passwordConfirm,
                               HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/demo/login";
        }

        if (password.equals(passwordConfirm)) {
            Member member = memberRepository.findByUsername(username);
            member.setPassword(passwordEncoder.encode(password));
            memberRepository.save(member);
        }
        return "redirect:/demo/member-history";
    }

    // 회원 탈퇴 처리
    @PostMapping("/delete-member")
    public String deleteMember(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/demo/login";
        }

        Member member = memberRepository.findByUsername(username);
        memberRepository.delete(member);
        session.invalidate();
        return "redirect:/demo/login";
    }

    // 관심 저장 취소 처리
    @PostMapping("/cancel-save/{id}")
    public String cancelSave(@PathVariable("id") Long id) {
        saveListRepository.deleteById(id);
        return "redirect:/demo/member-history";
    }

    // 지원 취소 처리
    @PostMapping("/cancel-apply/{id}")
    public String cancelApply(@PathVariable("id") Long id) {
        appListRepository.deleteById(id);
        return "redirect:/demo/member-history";
    }
}
