package com.example.jobapi.repository;

import com.example.jobapi.dto.JobPostingDto;
import com.example.jobapi.dto.SearchCondition;
import com.example.jobapi.entity.QJobPosting;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.querydsl.core.types.dsl.Expressions.allOf;
import static org.springframework.util.StringUtils.hasText;

public class JobPostingRepositoryImpl implements JobPostingRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public JobPostingRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<JobPostingDto> search(SearchCondition condition) {
        QJobPosting jobPosting = QJobPosting.jobPosting;

        return queryFactory
                .select(Projections.bean(JobPostingDto.class,
                        jobPosting.id, jobPosting.title, jobPosting.company, jobPosting.location))
                .from(jobPosting)
                .where(
                        titleEq(condition.getTitle()),
                        companyEq(condition.getCompany()),
                        locationEq(condition.getLocation())
                )
                .fetch();
    }

    @Override
    public Page<JobPostingDto> searchPage(SearchCondition condition, Pageable pageable) {
        QJobPosting jobPosting = QJobPosting.jobPosting;

        BooleanBuilder builder = new BooleanBuilder();

        if (hasText(condition.getTitle())) {
            builder.or(jobPosting.title.containsIgnoreCase(condition.getTitle()));
        }
        if (hasText(condition.getCompany())) {
            builder.or(jobPosting.company.containsIgnoreCase(condition.getCompany()));
        }
        if (hasText(condition.getLocation())) {
            builder.or(jobPosting.location.containsIgnoreCase(condition.getLocation()));
        }

        QueryResults<JobPostingDto> results = queryFactory
                .select(Projections.bean(JobPostingDto.class,
                        jobPosting.id, jobPosting.title, jobPosting.company, jobPosting.location))
                .from(jobPosting)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetchResults();

        return new PageImpl<>(results.getResults(), pageable, results.getTotal());
    }

    private BooleanExpression titleEq(String title) {
        return hasText(title) ? QJobPosting.jobPosting.title.eq(title) : null;
    }

    private BooleanExpression companyEq(String company) {
        return hasText(company) ? QJobPosting.jobPosting.company.eq(company) : null;
    }

    private BooleanExpression locationEq(String location) {
        return hasText(location) ? QJobPosting.jobPosting.location.eq(location) : null;
    }
}
