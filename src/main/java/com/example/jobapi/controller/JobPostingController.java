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
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

    private Member getLoginMember(HttpSession session) {
        return (Member) session.getAttribute("loginMember");
    }

    @GetMapping("/list")
    public String listJobPostings(@RequestParam(value = "keyword", required = false) String keyword,
                                  @RequestParam(value = "pagenum", defaultValue = "0") int pagenum,
                                  Model model,
                                  @SessionAttribute(name = "loginMember", required = false) Member sessionMember
    ) {
        model.addAttribute("sessionMember", sessionMember);
        model.addAttribute("keyword", keyword);
        model.addAttribute("pagenum", pagenum);

        SearchCondition condition = new SearchCondition();
        if (keyword != null) {
            condition.setTitle(keyword);
            condition.setCompany(keyword);
        }

        PageRequest pageRequest = PageRequest.of(pagenum, 20);
        Page<JobPostingDto> jobPostings = jobPostingRepository.searchPage(condition, pageRequest);
        model.addAttribute("jobPostings", jobPostings);
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

        boolean canEdit = loginMember != null && jobPosting.getAuthor().getId().equals(loginMember.getId());
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


