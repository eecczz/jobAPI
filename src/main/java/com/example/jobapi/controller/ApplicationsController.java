package com.example.jobapi.controller;

import com.example.jobapi.entity.AppList;
import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.entity.Member;
import com.example.jobapi.exception.ForbiddenException;
import com.example.jobapi.exception.InvalidRequestException;
import com.example.jobapi.exception.NotFoundException;
import com.example.jobapi.exception.UnauthorizedException;
import com.example.jobapi.repository.AppListRepository;
import com.example.jobapi.repository.JobPostingRepository;
import com.example.jobapi.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/applications")
public class ApplicationsController {

    @Autowired
    private JobPostingRepository jobPostingRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AppListRepository appListRepository;

    // 지원 등록
    @PostMapping("/{id}")
    public String applyForJob(@PathVariable("id") Long jobId, HttpSession session) {
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

        // 중복 지원 방지
        boolean alreadyApplied = appListRepository.existsByMemberAndJobPosting(member, jobPosting);
        if (alreadyApplied) {
            throw new ForbiddenException("이미 지원한 공고입니다.");
        }

        // 지원 등록
        AppList appList = new AppList();
        appList.setMember(member);
        appList.setJobPosting(jobPosting);
        appListRepository.save(appList);

        return "redirect:/jobs";
    }

    // 지원 취소 처리
    @DeleteMapping("/{id}")
    public String cancelApply(@PathVariable("id") Long id, HttpSession session) {
        String username = (String) session.getAttribute("username");
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        // 로그인 확인
        if (loggedIn == null || !loggedIn) {
            throw new UnauthorizedException("로그인이 필요합니다.");
        }

        // 지원 내역 조회
        AppList appList = appListRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("해당 지원 내역을 찾을 수 없습니다."));

        // 지원 취소 권한 확인
        if (!appList.getMember().getUsername().equals(username)) {
            throw new ForbiddenException("지원 취소 권한이 없습니다.");
        }

        // JobPosting의 cancel 값 확인
        JobPosting jobPosting = appList.getJobPosting();
        if (!jobPosting.getCancel()) {
            throw new InvalidRequestException("해당 지원은 취소할 수 없습니다. (취소가 허용되지 않은 공고)");
        }

        // 지원 취소 처리
        appListRepository.delete(appList);
        return "redirect:/auth/profile";
    }

}
