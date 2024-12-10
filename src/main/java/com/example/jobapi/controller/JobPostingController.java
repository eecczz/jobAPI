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
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
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

    @PostMapping("/crawl")
    public String crawlSaramin(@RequestParam String keyword, @RequestParam int pages) {
        crawlingService.crawlSaramin(keyword, pages);
        return "Crawling completed for keyword: " + keyword;
    }

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
            @RequestParam(value = "pagenum", defaultValue = "0") int pagenum
    ) {
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

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> readJobPosting(@PathVariable("id") Long id, HttpSession session) {
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
    @PutMapping("/{id}")
    public String modifyJobPosting(@PathVariable("id") Long id, @ModelAttribute JobPosting jobPosting, HttpSession session) {
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

        return "redirect:/jobs";
    }

    @DeleteMapping("/{id}")
    public String deleteJobPosting(@PathVariable("id") Long id, HttpSession session) {
        Member loggedInMember = getLoginMember(session);

        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Job posting not found"));

        if (!jobPosting.getAuthor().equals(loggedInMember)) {
            throw new ForbiddenException("You do not have permission to delete this job posting");
        }

        jobPostingRepository.delete(jobPosting);
        return "redirect:/jobs";
    }

    @GetMapping("/data")
    public ResponseEntity<?> getAllMembersJobData() {
        try {
            // 모든 회원 목록 조회
            List<Member> members = memberRepository.findAll();

            // 회원별 지원 목록과 관심 등록 목록 매핑
            List<Map<String, Object>> result = members.stream().map(member -> {
                Map<String, Object> memberData = new HashMap<>();
                memberData.put("username", member.getUsername());
                memberData.put("appliedJobs", member.getAppLists()); // 지원 목록
                memberData.put("likedJobs", member.getSaveLists());     // 관심 등록 목록
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

