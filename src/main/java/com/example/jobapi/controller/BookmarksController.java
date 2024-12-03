package com.example.jobapi.controller;

import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.entity.Member;
import com.example.jobapi.entity.SaveList;
import com.example.jobapi.exception.ForbiddenException;
import com.example.jobapi.exception.InvalidRequestException;
import com.example.jobapi.exception.NotFoundException;
import com.example.jobapi.exception.UnauthorizedException;
import com.example.jobapi.repository.JobPostingRepository;
import com.example.jobapi.repository.MemberRepository;
import com.example.jobapi.repository.SaveListRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/bookmarks")
public class BookmarksController {

    @Autowired
    private JobPostingRepository jobPostingRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private SaveListRepository saveListRepository;

    // 관심 등록
    @PostMapping("/{id}")
    public String saveJobPosting(@PathVariable("id") Long jobId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        // 로그인 확인
        if (loggedIn == null || !loggedIn) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new NotFoundException("해당 공고를 찾을 수 없습니다."));

        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            throw new UnauthorizedException("사용자 정보를 찾을 수 없습니다.");
        }

        // 중복 관심 등록 방지
        boolean alreadySaved = saveListRepository.existsByMemberAndJobPosting(member, jobPosting);
        if (alreadySaved) {
            throw new ForbiddenException("이미 관심 등록된 공고입니다.");
        }

        // 관심 등록
        SaveList saveList = new SaveList();
        saveList.setMember(member);
        saveList.setJobPosting(jobPosting);
        saveListRepository.save(saveList);

        return "redirect:/jobs";
    }

    // 관심 저장 취소 처리
    @DeleteMapping("/{id}")
    public String cancelSave(@PathVariable("id") Long id, HttpSession session) {
        String username = (String) session.getAttribute("username");
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        // 로그인 확인
        if (loggedIn == null || !loggedIn) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        SaveList saveList = saveListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 관심 등록 내역을 찾을 수 없습니다."));

        // 사용자 권한 확인
        if (!saveList.getMember().getUsername().equals(username)) {
            throw new ForbiddenException("관심 등록 취소 권한이 없습니다.");
        }

        saveListRepository.delete(saveList);
        return "redirect:/auth/profile";
    }
}
