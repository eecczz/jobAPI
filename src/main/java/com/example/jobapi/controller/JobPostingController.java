package com.example.jobapi.controller;

import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class JobPostingController {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @GetMapping("/demo/list")
    public String listJobPostings(@RequestParam(value = "page", defaultValue = "1") int page, Model model) {
        int pageSize = 20;
        Pageable pageable = PageRequest.of(page - 1, pageSize);
        Page<JobPosting> jobPostingsPage = jobPostingRepository.findAll(pageable);

        model.addAttribute("jobPostingsPage", jobPostingsPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", jobPostingsPage.getTotalPages());
        return "list";
    }

}
