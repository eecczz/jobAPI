package com.example.jobapi.controller;

import com.example.jobapi.entity.Member;
import com.example.jobapi.repository.AppListRepository;
import com.example.jobapi.repository.MemberRepository;
import com.example.jobapi.repository.SaveListRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/demo")
public class MemberController {
    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SaveListRepository saveListRepository;

    @Autowired
    private AppListRepository appListRepository;

    @GetMapping("/signup")
    public String signupForm() {
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@ModelAttribute Member member) {
        memberRepository.save(member);
        return "redirect:/demo/signin";
    }

    @GetMapping("/signin")
    public String signinForm() {
        return "signin";
    }

    @PostMapping("/signin")
    public String signin(@RequestParam("username") String username, @RequestParam("password") String password, HttpSession session) {
        Member member = memberRepository.findByUsername(username);
        if (member != null && member.getPassword().equals(password)) {
            session.setAttribute("loginMember", member);
            return "redirect:/demo/list";
        }
        return "signin";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/demo/list";
    }

    @GetMapping("/member-history")
    public String memberHistory(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        model.addAttribute("member", loginMember);
        model.addAttribute("saveList", saveListRepository.findByMember(loginMember));
        model.addAttribute("appList", appListRepository.findByMember(loginMember));
        return "member-history";
    }

    @GetMapping("/member-modify")
    public String modifyMemberForm() {
        return "member-modify";
    }

    @PostMapping("/member-modify")
    public String modifyMember(@RequestParam("password") String password,
                               @RequestParam("passwordConfirm") String passwordConfirm,
                               HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");

        if (password.equals(passwordConfirm)) {
            loginMember.setPassword(password);
            memberRepository.save(loginMember);
        }
        return "redirect:/demo/member-history";
    }

    @PostMapping("/delete-member")
    public String deleteMember(HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        memberRepository.deleteById(loginMember.getId());
        session.invalidate();
        return "redirect:/demo/list";
    }

    @PostMapping("/cancel-save/{id}")
    public String cancelSave(@PathVariable("id") Long id) {
        saveListRepository.deleteById(id);
        return "redirect:/demo/member-history";
    }

    @PostMapping("/cancel-apply/{id}")
    public String cancelApply(@PathVariable("id") Long id) {
        appListRepository.deleteById(id);
        return "redirect:/demo/member-history";
    }
}
