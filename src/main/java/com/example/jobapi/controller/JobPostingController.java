package com.example.jobapi.controller;

import com.example.jobapi.dto.JobPostingDto;
import com.example.jobapi.dto.SearchCondition;
import com.example.jobapi.entity.AppList;
import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.entity.Member;
import com.example.jobapi.entity.SaveList;
import com.example.jobapi.repository.AppListRepository;
import com.example.jobapi.repository.JobPostingRepository;
import com.example.jobapi.repository.SaveListRepository;
import com.example.jobapi.service.SaraminCrawlingService;
import com.example.jobapi.specification.JobPostingSpecification;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    private Member getLoginMember(HttpSession session) {
        return (Member) session.getAttribute("loginMember");
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

        // Pageable 생성
        PageRequest pageRequest = PageRequest.of(pagenum, 20, sort);

        // Repository 호출
        Page<JobPosting> jobPostings = crawlingService.getFilteredJobPostings(
                location, experience, salary, sector, sortOrder, pagenum, 20
        );
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
    public String showRegisterForm(Model model) {
        model.addAttribute("jobPosting", new JobPosting());
        return "register";
    }

    @PostMapping("/register")
    public String registerJobPosting(@ModelAttribute JobPosting jobPosting, HttpSession session) {
        Member loginMember = getLoginMember(session);
        jobPosting.setAuthor(loginMember);
        jobPostingRepository.save(jobPosting);
        return "redirect:/demo/list";
    }

    @GetMapping("/read/{id}")
    public String readJobPosting(@PathVariable("id") Long id, Model model, HttpSession session) {
        JobPosting jobPosting = jobPostingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid job posting ID"));
        Member loginMember = getLoginMember(session);
        model.addAttribute("jobPosting", jobPosting);

        boolean canEdit = loginMember != null && jobPosting.getAuthor() != null && jobPosting.getAuthor().getId().equals(loginMember.getId());
        model.addAttribute("canEdit", canEdit);

        return "read";
    }

    @GetMapping("/modify/{id}")
    public String showModifyForm(@PathVariable("id") Long id, Model model) {
        Optional<JobPosting> jobPosting = jobPostingRepository.findById(id);
        jobPosting.ifPresent(value -> model.addAttribute("jobPosting", value));
        return "modify";
    }

    @PostMapping("/modify/{id}")
    public String modifyJobPosting(@PathVariable("id") Long id, @ModelAttribute JobPosting jobPosting) {
        jobPostingRepository.findById(id).ifPresent(existingJobPosting -> {
            existingJobPosting.setTitle(jobPosting.getTitle());
            existingJobPosting.setCompany(jobPosting.getCompany());
            existingJobPosting.setLocation(jobPosting.getLocation());
            existingJobPosting.setClosingDate(jobPosting.getClosingDate());
            existingJobPosting.setUrl(jobPosting.getUrl());
            jobPostingRepository.save(existingJobPosting);
        });
        return "redirect:/demo/list";
    }

    @PostMapping("/delete/{id}")
    public String deleteJobPosting(@PathVariable("id") Long id) {
        jobPostingRepository.deleteById(id);
        return "redirect:/demo/list";
    }

    @PostMapping("/save/{id}")
    public String saveJobPosting(@PathVariable("id") Long jobId, HttpSession session) {
        Member loginMember = getLoginMember(session);
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid job posting ID"));

        SaveList saveList = new SaveList();
        saveList.setMember(loginMember);
        saveList.setJobPosting(jobPosting);

        saveListRepository.save(saveList);
        return "redirect:/demo/list";
    }

    @PostMapping("/apply/{id}")
    public String applyForJob(@PathVariable("id") Long jobId, HttpSession session) {
        Member loginMember = getLoginMember(session);
        JobPosting jobPosting = jobPostingRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid job posting ID"));

        AppList appList = new AppList();
        appList.setMember(loginMember);
        appList.setJobPosting(jobPosting);

        appListRepository.save(appList);
        return "redirect:/demo/list";
    }
}


