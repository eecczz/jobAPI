package com.example.jobapi.controller;

import com.example.jobapi.dto.JobPostingDto;
import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.entity.Member;
import com.example.jobapi.exception.ForbiddenException;
import com.example.jobapi.exception.NotFoundException;
import com.example.jobapi.exception.UnauthorizedException;
import com.example.jobapi.repository.JobPostingRepository;
import com.example.jobapi.repository.MemberRepository;
import com.example.jobapi.service.SaraminCrawlingService;
import com.example.jobapi.specification.JobPostingSpecification;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/jobs")
public class JobPostingController {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private SaraminCrawlingService crawlingService;

    @Autowired
    private MemberRepository memberRepository;

    private Member getLoginMember(HttpSession session) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            throw new UnauthorizedException("User is not logged in");
        }

        Member member = memberRepository.findByUsername(username);
        if (member == null) {
            throw new UnauthorizedException("User not found");
        }

        return member;
    }

    @Operation(summary = "사람인 크롤링", description = "사람인에서 특정 키워드로 공고를 크롤링합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "크롤링 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @PostMapping("/crawl")
    public ResponseEntity<String> crawlSaramin(
            @Parameter(description = "크롤링할 키워드", required = true) @RequestParam String keyword,
            @Parameter(description = "크롤링할 페이지 수", required = true) @RequestParam int pages) {
        crawlingService.crawlSaramin(keyword, pages);
        return ResponseEntity.ok("Crawling completed for keyword: " + keyword);
    }

    @Operation(summary = "채용 공고 목록 조회", description = "필터 조건과 검색어를 바탕으로 채용 공고 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 파라미터")
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> listJobPostings(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "company", required = false) String company,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "sector", required = false) String sector,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "experience", required = false) String experience,
            @RequestParam(value = "salary", required = false) String salary,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            @RequestParam(value = "pagenum", defaultValue = "0") int pagenum) {
        Specification<JobPosting> spec = Specification.where(
                        JobPostingSpecification.hasKeyword(keyword))
                .and(JobPostingSpecification.hasCompany(company))
                .and(JobPostingSpecification.hasPosition(position))
                .and(JobPostingSpecification.hasSector(sector))
                .and(JobPostingSpecification.hasLocation(location))
                .and(JobPostingSpecification.hasExperience(experience))
                .and(JobPostingSpecification.hasSalary(salary));

        Sort sort = Sort.by(Sort.Direction.DESC, "closingDate");
        if ("close".equals(sortOrder)) {
            sort = Sort.by(Sort.Direction.ASC, "closingDate");
        } else if ("salary".equals(sortOrder)) {
            sort = Sort.by(Sort.Direction.DESC, "salary");
        } else if ("experience".equals(sortOrder)) {
            sort = Sort.by(Sort.Direction.ASC, "experience");
        }

        Page<JobPosting> jobPostings = jobPostingRepository.findAll(spec, PageRequest.of(pagenum, 20, sort));
        Page<JobPostingDto> jobPostingsDto = jobPostings.map(jobPosting -> {
            JobPostingDto dto = new JobPostingDto();
            dto.setId(jobPosting.getId());
            dto.setTitle(jobPosting.getTitle());
            dto.setCompany(jobPosting.getCompany());
            dto.setLocation(jobPosting.getLocation());
            dto.setClosingDate(jobPosting.getClosingDate());
            dto.setUrl(jobPosting.getUrl());
            dto.setSalary(jobPosting.getSalary());
            dto.setExperience(jobPosting.getExperience());
            dto.setSector(jobPosting.getSector());
            return dto;
        });

        Map<String, Object> response = new HashMap<>();
        response.put("jobPostings", jobPostingsDto);
        response.put("sortOrder", sortOrder);
        response.put("pagenum", pagenum);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "채용 공고 상세 조회", description = "특정 채용 공고의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 공고를 찾을 수 없음")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> readJobPosting(
            @Parameter(description = "조회할 채용 공고의 ID", required = true) @PathVariable("id") Long id,
            HttpSession session) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job posting not found"));

        Member loggedInMember = getLoginMember(session);
        boolean canEdit = jobPosting.getAuthor() != null && jobPosting.getAuthor().equals(loggedInMember);

        Map<String, Object> response = new HashMap<>();
        response.put("jobPosting", jobPosting);
        response.put("canEdit", canEdit);

        // Increase view count
        jobPosting.setView(jobPosting.getView() + 1);
        jobPostingRepository.save(jobPosting);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "채용 공고 수정", description = "특정 채용 공고를 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "수정 권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 공고를 찾을 수 없음")
    })
    @PutMapping("/{id}")
    public ResponseEntity<String> modifyJobPosting(
            @Parameter(description = "수정할 채용 공고의 ID", required = true) @PathVariable("id") Long id,
            @RequestBody JobPosting jobPosting, HttpSession session) {
        Member loggedInMember = getLoginMember(session);

        jobPostingRepository.findById(id).ifPresentOrElse(existingJobPosting -> {
            if (!existingJobPosting.getAuthor().equals(loggedInMember)) {
                throw new ForbiddenException("You do not have permission to modify this job posting");
            }

            existingJobPosting.setTitle(jobPosting.getTitle());
            existingJobPosting.setCompany(jobPosting.getCompany());
            existingJobPosting.setLocation(jobPosting.getLocation());
            existingJobPosting.setClosingDate(jobPosting.getClosingDate());
            existingJobPosting.setUrl(jobPosting.getUrl());
            jobPostingRepository.save(existingJobPosting);
        }, () -> {
            throw new NotFoundException("Job posting not found");
        });

        return ResponseEntity.ok("Job posting updated successfully.");
    }

    @Operation(summary = "채용 공고 삭제", description = "특정 채용 공고를 삭제합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
            @ApiResponse(responseCode = "404", description = "해당 공고를 찾을 수 없음")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteJobPosting(
            @Parameter(description = "삭제할 채용 공고의 ID", required = true) @PathVariable("id") Long id,
            HttpSession session) {
        Member loggedInMember = getLoginMember(session);

        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job posting not found"));

        if (!jobPosting.getAuthor().equals(loggedInMember)) {
            throw new ForbiddenException("You do not have permission to delete this job posting");
        }

        jobPostingRepository.delete(jobPosting);
        return ResponseEntity.ok("Job posting deleted successfully.");
    }

    @Operation(summary = "회원별 채용 데이터 조회", description = "모든 회원의 지원 내역과 관심 공고를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "500", description = "서버 에러")
    })
    @GetMapping("/data")
    public ResponseEntity<?> getAllMembersJobData() {
        try {
            List<Member> members = memberRepository.findAll();

            List<Map<String, Object>> result = members.stream().map(member -> {
                Map<String, Object> memberData = new HashMap<>();
                memberData.put("username", member.getUsername());
                memberData.put("appliedJobs", member.getAppLists());
                memberData.put("likedJobs", member.getSaveLists());
                return memberData;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "데이터를 가져오는 도중 문제가 발생했습니다."));
        }
    }
}
