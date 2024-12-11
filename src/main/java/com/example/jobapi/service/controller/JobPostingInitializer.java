package com.example.jobapi.service.controller;

import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.repository.JobPostingRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class JobPostingInitializer implements CommandLineRunner {

    private final JobPostingRepository jobPostingRepository;

    public JobPostingInitializer(JobPostingRepository jobPostingRepository) {
        this.jobPostingRepository = jobPostingRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        List<JobPosting> postings = new ArrayList<>();

        // 샘플 데이터 100개 추가
        postings.add(new JobPosting(
                true, null, 0L, "~ 02/07(금)", "(주)마크클라우드", null, "대졸↑",
                "인턴직", "경력무관", "서울 강남구", "IT개발·데이터 스크랩 TOP100",
                "Python, 딥러닝, 머신러닝, 빅데이터, 데이터분석가 외 등록일 24/12/09",
                "AI 개발(Python), 데이터분석 및 사업 기획 모집(인턴)",
                "https://www.saramin.co.kr/zf_user/jobs/relay/view?view_type=search&rec_idx=49551418&location=ts&searchword=python&searchType=search&paid_fl=n&search_uuid=e05876af-b5d3-4d5a-aa65-8f040608e37a"
        ));

        for (int i = 2; i <= 100; i++) {
            postings.add(new JobPosting(
                    true, null, 0L, "~ 12/" + (i % 31 + 1) + "(금)",
                    "회사명 " + i, null, "학력무관", "정규직", "신입",
                    "서울 지역 " + i, "IT개발·데이터 스크랩 급상승",
                    "Java, Spring, Python, 데이터 분석 등 등록일 24/12/" + (i % 31 + 1),
                    "Job Posting Title " + i,
                    "https://www.example.com/job/" + i
            ));
        }

        jobPostingRepository.saveAll(postings);

        System.out.println("Job postings initialized with 100 entries.");
    }
}
