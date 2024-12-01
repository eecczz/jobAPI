package com.example.jobapi.repository;

import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.entity.SaveList;
import com.example.jobapi.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SaveListRepository extends JpaRepository<SaveList, Long> {
    // Finds all saved jobs for a given member
    List<SaveList> findByMember(Member member);
    boolean existsByMemberAndJobPosting(Member member, JobPosting jobPosting);
}
