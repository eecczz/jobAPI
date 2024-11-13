package com.example.jobapi.repository;

import com.example.jobapi.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    // 중복 확인 메서드
    boolean existsByUrl(String url);
}
