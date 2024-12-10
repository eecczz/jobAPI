package com.example.jobapi.service;

import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.repository.JobPostingRepository;
import com.example.jobapi.specification.JobPostingSpecification;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SaraminCrawlingService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    public Page<JobPosting> getFilteredJobPostings(String location, String experience, String salary, String sector, String sortOrder, int page, int size) {
        Specification<JobPosting> spec = Specification.where(
                JobPostingSpecification.hasLocation(location)
                        .and(JobPostingSpecification.hasExperience(experience))
                        .and(JobPostingSpecification.hasSalary(salary))
                        .and(JobPostingSpecification.hasSector(sector))
        );

        Sort sort = Sort.by(Sort.Direction.ASC, "id"); // 기본 정렬

        if (sortOrder != null) {
            switch (sortOrder) {
                case "close":
                    sort = Sort.by(Sort.Direction.ASC, "closingDate");
                    break;
                case "salary":
                    sort = Sort.by(Sort.Direction.DESC, "salary");
                    break;
                case "experience":
                    sort = Sort.by(Sort.Direction.DESC, "experience");
                    break;
                default:
                    // 기본 정렬 옵션 처리
                    sort = Sort.by(Sort.Direction.ASC, "id");
                    break;
            }
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        return jobPostingRepository.findAll(spec, pageable);
    }


    public void crawlSaramin(String keyword, int pages) {
        List<JobPosting> jobPostings = new ArrayList<>();
        String baseUrl = "https://www.saramin.co.kr/zf_user/search/recruit?searchType=search&searchword=";

        for (int page = 1; page <= pages; page++) {
            try {
                String url = baseUrl + keyword + "&recruitPage=" + page;
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                        .timeout(10000)
                        .get();

                Elements jobListings = doc.select(".item_recruit");

                for (Element job : jobListings) {
                    try {
                        // 회사명
                        String company = job.selectFirst(".corp_name a").text();

                        // 제목
                        String title = job.selectFirst(".job_tit a").text();

                        // 링크
                        String link = "https://www.saramin.co.kr" + job.selectFirst(".job_tit a").attr("href");

                        // 지역, 경력, 학력, 고용형태
                        Elements conditions = job.select(".job_condition span");
                        String location = conditions.size() > 0 ? conditions.get(0).text() : "";
                        String experience = conditions.size() > 1 ? conditions.get(1).text() : "";
                        String education = conditions.size() > 2 ? conditions.get(2).text() : "";
                        String employmentType = conditions.size() > 3 ? conditions.get(3).text() : "";

                        // 마감일
                        String deadline = job.selectFirst(".job_date .date").text();

                        // 직무 분야
                        Element jobSector = job.selectFirst(".job_sector");
                        String sector = jobSector != null ? jobSector.text() : "";

                        // 연봉
                        Element salaryBadge = job.selectFirst(".area_badge .badge");
                        String salary = salaryBadge != null ? salaryBadge.text() : "";

                        JobPosting jobPosting = new JobPosting();
                        jobPosting.setTitle(title);
                        jobPosting.setCompany(company);
                        jobPosting.setLocation(location);
                        jobPosting.setExperience(experience);
                        jobPosting.setEducation(education);
                        jobPosting.setEmploymentType(employmentType);
                        jobPosting.setClosingDate(deadline);
                        jobPosting.setSector(sector);
                        jobPosting.setSalary(salary);
                        jobPosting.setUrl(link);

                        // 중복 확인 후 추가
                        if (!jobPostingRepository.existsByUrl(link)) {
                            jobPostings.add(jobPosting);
                        }
                    } catch (Exception e) {
                        System.out.println("Error parsing job posting: " + e.getMessage());
                    }
                }

                System.out.println("Page " + page + " crawled successfully. Found " + jobListings.size() + " jobs.");

                // 서버 부하 방지를 위해 타임간격 추가
                TimeUnit.SECONDS.sleep(2);

            } catch (IOException | InterruptedException e) {
                System.out.println("Error fetching page " + page + ": " + e.getMessage());
            }
        }

        // 저장
        jobPostingRepository.saveAll(jobPostings);
        System.out.println("총 " + jobPostings.size() + "개의 채용공고가 저장되었습니다.");
    }
}
