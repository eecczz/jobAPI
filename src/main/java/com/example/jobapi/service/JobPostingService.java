package com.example.jobapi.service;

import com.example.jobapi.dto.JobPostingDto;
import com.example.jobapi.dto.SearchCondition;
import com.example.jobapi.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobPostingService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    public List<JobPostingDto> searchJobPostings(SearchCondition condition) {
        return jobPostingRepository.search(condition);
    }

    public Page<JobPostingDto> searchJobPostingsPage(SearchCondition condition, Pageable pageable) {
        return jobPostingRepository.searchPage(condition, pageable);
    }
}
