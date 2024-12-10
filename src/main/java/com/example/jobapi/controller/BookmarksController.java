package com.example.jobapi.controller;

import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.entity.Member;
import com.example.jobapi.entity.SaveList;
import com.example.jobapi.exception.ForbiddenException;
import com.example.jobapi.exception.NotFoundException;
import com.example.jobapi.exception.UnauthorizedException;
import com.example.jobapi.repository.JobPostingRepository;
import com.example.jobapi.repository.MemberRepository;
import com.example.jobapi.repository.SaveListRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookmarks")
public class BookmarksController {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private SaveListRepository saveListRepository;

    @Operation(summary = "관심 등록", description = "특정 채용 공고를 관심 목록에 추가합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관심 등록 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "이미 관심 등록된 공고입니다."),
            @ApiResponse(responseCode = "404", description = "해당 채용 공고를 찾을 수 없습니다.")
    })
    @PostMapping("/{id}")
    public ResponseEntity<?> saveJobPosting(
            @Parameter(description = "관심 등록할 채용 공고의 ID", required = true)
            @PathVariable("id") Long jobId, HttpSession session) {
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

        return ResponseEntity.ok("관심 공고가 성공적으로 등록되었습니다.");
    }

    @Operation(summary = "관심 등록 취소", description = "특정 채용 공고를 관심 목록에서 제거합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "관심 등록 취소 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @ApiResponse(responseCode = "403", description = "취소 권한이 없습니다."),
            @ApiResponse(responseCode = "404", description = "해당 관심 등록 내역을 찾을 수 없습니다.")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelSave(
            @Parameter(description = "관심 등록을 취소할 ID", required = true)
            @PathVariable("id") Long id, HttpSession session) {
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
        return ResponseEntity.ok("관심 등록이 성공적으로 취소되었습니다.");
    }
}
