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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class SaraminCrawlingService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    private static final int THREAD_POOL_SIZE = 10; // 병렬 처리 스레드 수

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

        // 병렬 작업용 스레드풀 생성
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (int page = 1; page <= pages; page++) {
            final int currentPage = page;
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    String url = baseUrl + keyword + "&recruitPage=" + currentPage;
                    System.out.println("Fetching URL: " + url);

                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                            .timeout(20000) // 타임아웃 20초
                            .get();

                    Elements jobListings = doc.select(".item_recruit");

                    for (Element job : jobListings) {
                        try {
                            String company = job.selectFirst(".corp_name a").text();
                            String title = job.selectFirst(".job_tit a").text();
                            String link = "https://www.saramin.co.kr" + job.selectFirst(".job_tit a").attr("href");

                            Elements conditions = job.select(".job_condition span");
                            String location = conditions.size() > 0 ? conditions.get(0).text() : "";
                            String experience = conditions.size() > 1 ? conditions.get(1).text() : "";
                            String education = conditions.size() > 2 ? conditions.get(2).text() : "";
                            String employmentType = conditions.size() > 3 ? conditions.get(3).text() : "";

                            String deadline = job.selectFirst(".job_date .date").text();
                            Element jobSector = job.selectFirst(".job_sector");
                            String sector = jobSector != null ? jobSector.text() : "";

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

                            synchronized (jobPostings) { // 동기화 처리
                                if (!jobPostingRepository.existsByUrl(link)) {
                                    jobPostings.add(jobPosting);
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("Error parsing job posting: " + e.getMessage());
                        }
                    }

                    System.out.println("Page " + currentPage + " crawled successfully. Found " + jobListings.size() + " jobs.");
                } catch (IOException e) {
                    System.out.println("Error fetching page " + currentPage + ": " + e.getMessage());
                }
            }, executor);

            futures.add(future);
        }

        // 모든 병렬 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 스레드풀 종료
        executor.shutdown();

        // 결과 저장
        jobPostingRepository.saveAll(jobPostings);
        System.out.println("총 " + jobPostings.size() + "개의 채용공고가 저장되었습니다.");
    }
}
