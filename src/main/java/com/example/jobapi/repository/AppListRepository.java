package com.example.jobapi.repository;

import com.example.jobapi.entity.AppList;
import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AppListRepository extends JpaRepository<AppList, Long> {
    // Finds all applied jobs for a given member
    List<AppList> findByMember(Member member);
    boolean existsByMemberAndJobPosting(Member member, JobPosting jobPosting);
}
