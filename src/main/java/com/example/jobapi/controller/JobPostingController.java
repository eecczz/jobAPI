package com.example.jobapi.controller;

import com.example.jobapi.dto.JobPostingDto;
import com.example.jobapi.dto.SearchCondition;
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
    public String listJobPostings(@RequestParam(value = "keyword", required = false) String keyword,
                                  @RequestParam(value = "pagenum", defaultValue = "0") int pagenum, Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("pagenum", pagenum);
        SearchCondition condition = new SearchCondition();

        if (keyword != null) {
            condition.setTitle(keyword);
            condition.setCompany(keyword);
        }

        // 페이지 요청 설정
        PageRequest pageRequest = PageRequest.of(pagenum, 20);

        // 검색 조건을 통한 데이터 조회
        Page<JobPostingDto> jobPostings = jobPostingRepository.searchPage(condition, pageRequest);
        model.addAttribute("jobPostings", jobPostings);
        return "list";
    }

}
