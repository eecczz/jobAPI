package com.example.jobapi.controller;

import com.example.jobapi.dto.JobPostingDto;
import com.example.jobapi.dto.SearchCondition;
import com.example.jobapi.entity.AppList;
import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.entity.Member;
import com.example.jobapi.entity.SaveList;
import com.example.jobapi.repository.AppListRepository;
import com.example.jobapi.repository.JobPostingRepository;
import com.example.jobapi.repository.MemberRepository;
import com.example.jobapi.repository.SaveListRepository;
import com.example.jobapi.service.SaraminCrawlingService;
import com.example.jobapi.specification.JobPostingSpecification;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Controller
@RequestMapping("/demo")
public class JobPostingController {

    @Autowired
    private JobPostingRepository jobPostingRepository;
    @Autowired
    private SaveListRepository saveListRepository;
    @Autowired
    private AppListRepository appListRepository;
    @Autowired
    private SaraminCrawlingService crawlingService;
    @Autowired
    private MemberRepository memberRepository;

    // JWT 토큰에서 로그인된 Member를 추출하는 메서드
    private Member getLoginMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            return memberRepository.findByUsername(username);
        }
        return null;
    }

    @PostMapping("/crawl")
    public String crawlSaramin(@RequestParam String keyword, @RequestParam int pages) {
        crawlingService.crawlSaramin(keyword, pages);
        return "Crawling completed for keyword: " + keyword;
    }

    @GetMapping("/list")
    public String listJobPostings(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "company", required = false) String company,
            @RequestParam(value = "position", required = false) String position,
            @RequestParam(value = "sector", required = false) String sector,
            @RequestParam(value = "location", required = false) String location,
            @RequestParam(value = "experience", required = false) String experience,
            @RequestParam(value = "salary", required = false) String salary,
            @RequestParam(value = "sortOrder", required = false) String sortOrder,
            @RequestParam(value = "pagenum", defaultValue = "0") int pagenum,
            Model model
    ) {
        // Specification 생성
        Specification<JobPosting> spec = Specification.where(
                        JobPostingSpecification.hasKeyword(keyword))
                .and(JobPostingSpecification.hasCompany(company))
                .and(JobPostingSpecification.hasPosition(position))
                .and(JobPostingSpecification.hasSector(sector))
                .and(JobPostingSpecification.hasLocation(location))
                .and(JobPostingSpecification.hasExperience(experience))
                .and(JobPostingSpecification.hasSalary(salary));

        // 정렬 처리
        Sort sort = Sort.by(Sort.Direction.DESC, "closingDate"); // 기본값: 최신순
        if (sortOrder != null) {
            switch (sortOrder) {
                case "close":
                    sort = Sort.by(Sort.Direction.ASC, "closingDate");
                    break;
                case "salary":
                    sort = Sort.by(Sort.Direction.DESC, "salary");
                    break;
                case "experience":
                    sort = Sort.by(Sort.Direction.ASC, "experience");
                    break;
            }
        }

        // Repository 호출
        Page<JobPosting> jobPostings = jobPostingRepository.findAll(spec, PageRequest.of(pagenum, 20, sort));

        // JobPosting -> JobPostingDto로 매핑
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

        // Model에 데이터 추가
        model.addAttribute("jobPostings", jobPostingsDto);
        model.addAttribute("sortOrder", sortOrder);
        model.addAttribute("pagenum", pagenum);
        return "list";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model, HttpSession session) {
        model.addAttribute("jobPosting", new JobPosting());
        String username = (String) session.getAttribute("username");
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        if (loggedIn == null || !loggedIn) {
            return "redirect:/demo/login";
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerJobPosting(@ModelAttribute JobPosting jobPosting, HttpSession session) {
        String username = (String) session.getAttribute("username");
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        // 로그인 확인
        if (loggedIn == null || !loggedIn) {
            return "redirect:/demo/login";
        }

        Member author = memberRepository.findByUsername(username);
        if (author == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "작성자 정보를 찾을 수 없습니다.");
        }

        jobPosting.setAuthor(author);
        jobPostingRepository.save(jobPosting);
        return "redirect:/demo/list";
    }


    @GetMapping("/read/{id}")
    public String readJobPosting(@PathVariable("id") Long id, Model model, HttpSession session) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid job posting ID"));

        String username = (String) session.getAttribute("username");
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        model.addAttribute("jobPosting", jobPosting);

        // 작성자인지 확인
        boolean canEdit = jobPosting.getAuthor() != null && jobPosting.getAuthor().getUsername().equals(username);
        model.addAttribute("canEdit", canEdit);

        return "read";
    }

    @GetMapping("/modify/{id}")
    public String showModifyForm(@PathVariable("id") Long id, Model model, HttpSession session) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid job posting ID"));

        String username = (String) session.getAttribute("username");
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        if (loggedIn == null || !loggedIn) {
            return "redirect:/demo/login";
        }

        // 작성자인지 확인
        if (!jobPosting.getAuthor().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
        }

        model.addAttribute("jobPosting", jobPosting);
        return "modify";
    }

    @PostMapping("/modify/{id}")
    public String modifyJobPosting(@PathVariable("id") Long id, @ModelAttribute JobPosting jobPosting, HttpSession session) {
        String username = (String) session.getAttribute("username");
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        // 로그인 확인
        if (loggedIn == null || !loggedIn) {
            return "redirect:/demo/login";
        }

        jobPostingRepository.findById(id).ifPresentOrElse(existingJobPosting -> {
            // 작성자인지 확인
            Member author = existingJobPosting.getAuthor();
            if (author == null || !author.getUsername().equals(username)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "수정 권한이 없습니다.");
            }

            // 수정 작업
            existingJobPosting.setTitle(jobPosting.getTitle());
            existingJobPosting.setCompany(jobPosting.getCompany());
            existingJobPosting.setLocation(jobPosting.getLocation());
            existingJobPosting.setClosingDate(jobPosting.getClosingDate());
            existingJobPosting.setUrl(jobPosting.getUrl());
            jobPostingRepository.save(existingJobPosting);
        }, () -> {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "해당 공고를 찾을 수 없습니다.");
        });

        return "redirect:/demo/list";
    }


    @PostMapping("/delete/{id}")
    public String deleteJobPosting(@PathVariable("id") Long id, HttpSession session) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid job posting ID"));

        String username = (String) session.getAttribute("username");
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        if (loggedIn == null || !loggedIn) {
            return "redirect:/demo/login";
        }

        // 작성자인지 확인
        if (!jobPosting.getAuthor().getUsername().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "삭제 권한이 없습니다.");
        }

        jobPostingRepository.delete(jobPosting);
        return "redirect:/demo/list";
    }

    @PostMapping("/save/{id}")
    public String saveJobPosting(@PathVariable("id") Long jobId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        // 로그인 확인
        if (loggedIn == null || !loggedIn) {
            return "redirect:/demo/login";
        }

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid job posting ID"));

        Member member = memberRepository.findByUsername(username);

        // 중복 관심 등록 방지
        boolean alreadySaved = saveListRepository.existsByMemberAndJobPosting(member, jobPosting);
        if (alreadySaved) {
            throw new IllegalStateException("이미 관심 등록된 공고입니다.");
        }

        // 관심 등록
        SaveList saveList = new SaveList();
        saveList.setMember(member);
        saveList.setJobPosting(jobPosting);
        saveListRepository.save(saveList);

        return "redirect:/demo/list";
    }

    @PostMapping("/apply/{id}")
    public String applyForJob(@PathVariable("id") Long jobId, HttpSession session) {
        String username = (String) session.getAttribute("username");
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        // 로그인 확인
        if (loggedIn == null || !loggedIn) {
            return "redirect:/demo/login";
        }

        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid job posting ID"));

        Member member = memberRepository.findByUsername(username);

        // 중복 지원 방지
        boolean alreadyApplied = appListRepository.existsByMemberAndJobPosting(member, jobPosting);
        if (alreadyApplied) {
            throw new IllegalStateException("이미 지원한 공고입니다.");
        }

        // 지원 등록
        AppList appList = new AppList();
        appList.setMember(member);
        appList.setJobPosting(jobPosting);
        appListRepository.save(appList);

        return "redirect:/demo/list";
    }

}


