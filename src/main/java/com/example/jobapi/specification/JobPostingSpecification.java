package com.example.jobapi.specification;

import com.example.jobapi.entity.JobPosting;
import org.springframework.data.jpa.domain.Specification;

public class JobPostingSpecification {

    // 키워드 필터링
    public static Specification<JobPosting> hasKeyword(String keyword) {
        return (root, query, criteriaBuilder) -> {
            if (keyword == null || keyword.isEmpty()) {
                return criteriaBuilder.conjunction(); // 항상 true
            }
            return criteriaBuilder.like(root.get("title"), "%" + keyword + "%");
        };
    }

    // 회사명 필터링
    public static Specification<JobPosting> hasCompany(String company) {
        return (root, query, criteriaBuilder) -> {
            if (company == null || company.isEmpty()) {
                return criteriaBuilder.conjunction(); // 항상 true
            }
            return criteriaBuilder.like(root.get("company"), "%" + company + "%");
        };
    }

    // 포지션 필터링
    public static Specification<JobPosting> hasPosition(String position) {
        return (root, query, criteriaBuilder) -> {
            if (position == null || position.isEmpty()) {
                return criteriaBuilder.conjunction(); // 항상 true
            }
            return criteriaBuilder.like(root.get("position"), "%" + position + "%");
        };
    }

    // 위치 필터링
    public static Specification<JobPosting> hasLocation(String location) {
        return (root, query, criteriaBuilder) -> {
            if (location == null || location.isEmpty()) {
                return criteriaBuilder.conjunction(); // 항상 true
            }
            return criteriaBuilder.like(root.get("location"), "%" + location + "%");
        };
    }

    // 경력 필터링
    public static Specification<JobPosting> hasExperience(String experience) {
        return (root, query, criteriaBuilder) -> {
            if (experience == null || experience.isEmpty()) {
                return criteriaBuilder.conjunction(); // 항상 true
            }
            return criteriaBuilder.like(root.get("experience"), "%" + experience + "%");
        };
    }

    // 급여 필터링
    public static Specification<JobPosting> hasSalary(String salary) {
        return (root, query, criteriaBuilder) -> {
            if (salary == null || salary.isEmpty()) {
                return criteriaBuilder.conjunction(); // 항상 true
            }
            return criteriaBuilder.like(root.get("salary"), "%" + salary + "%");
        };
    }

    // 섹터(기술 스택) 필터링
    public static Specification<JobPosting> hasSector(String sector) {
        return (root, query, criteriaBuilder) -> {
            if (sector == null || sector.isEmpty()) {
                return criteriaBuilder.conjunction(); // 항상 true
            }
            return criteriaBuilder.like(root.get("sector"), "%" + sector + "%");
        };
    }
}
