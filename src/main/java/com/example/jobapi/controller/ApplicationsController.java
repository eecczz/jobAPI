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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/applications")
public class ApplicationsController {

    @Autowired
    private JobPostingRepository jobPostingRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private AppListRepository appListRepository;

    /**
     * 지원 등록
     */
    @Operation(summary = "채용 공고 지원하기", description = "특정 채용 공고에 지원을 등록합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지원이 성공적으로 등록되었습니다."),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "이미 지원한 공고입니다."),
            @ApiResponse(responseCode = "404", description = "해당 공고를 찾을 수 없습니다.")
    })
    @PostMapping("/{id}")
    public String applyForJob(
            @Parameter(description = "지원할 채용 공고의 ID", required = true)
            @PathVariable("id") Long jobId,
            HttpSession session) {

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

    /**
     * 지원 취소 처리
     */
    @Operation(summary = "지원 취소", description = "등록된 지원 내역을 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "지원이 성공적으로 취소되었습니다."),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "지원 취소 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "해당 지원 내역을 찾을 수 없습니다.")
    })
    @DeleteMapping("/{id}")
    public String cancelApply(
            @Parameter(description = "취소할 지원 내역의 ID", required = true)
            @PathVariable("id") Long id,
            HttpSession session) {

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
