package com.example.jobapi.service;

import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.repository.JobPostingRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class SaraminCrawlingService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    public void crawlSaramin(String keyword, int pages) {
        List<JobPosting> jobPostings = new ArrayList<>();
        String baseUrl = "https://www.saramin.co.kr/zf_user/search/recruit?searchType=search&searchword=";

        for (int page = 1; page <= pages; page++) {
            boolean success = false;
            int retries = 0;
            while (!success && retries < 5) { // 최대 5회 재시도
                try {
                    String url = baseUrl + keyword + "&recruitPage=" + page;
                    Document doc = Jsoup.connect(url)
                            .userAgent("Mozilla/5.0")
                            .get(); // 타임아웃 제거

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

                            if (!jobPostingRepository.existsByUrl(link)) {
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
                                jobPostings.add(jobPosting);
                            }
                        } catch (Exception e) {
                            System.out.println("Error parsing job posting: " + e.getMessage());
                        }
                    }

                    if (!jobPostings.isEmpty()) {
                        jobPostingRepository.saveAll(jobPostings);
                        jobPostings.clear();
                    }

                    System.out.println("Page " + page + " crawled successfully. Found " + jobListings.size() + " jobs.");
                    success = true;
                } catch (IOException e) {
                    retries++;
                    System.out.println("Error fetching page " + page + ", retrying (" + retries + "): " + e.getMessage());
                }
            }

            if (!success) {
                System.out.println("Failed to fetch page " + page + " after maximum retries.");
            }
        }

        System.out.println("크롤링 완료.");
    }
}
