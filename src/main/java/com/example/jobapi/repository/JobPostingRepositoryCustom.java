package com.example.jobapi.repository;

import com.example.jobapi.dto.JobPostingDto;
import com.example.jobapi.dto.SearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface JobPostingRepositoryCustom {
    List<JobPostingDto> search(SearchCondition condition);
    Page<JobPostingDto> searchPage(SearchCondition condition, Pageable pageable);
}
