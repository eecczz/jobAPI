package com.example.jobapi.Service;

import com.example.jobapi.entity.JobPosting;
import com.example.jobapi.repository.JobPostingRepository;
import jakarta.transaction.Transactional;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SaraminCrawlingService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Transactional // 트랜잭션 추가
    public void crawlSaramin() {
        System.setProperty("webdriver.chrome.driver", "C:\\Users\\swh01\\chromedriver-win64\\chromedriver.exe");
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

        try {
            driver.get("https://www.saramin.co.kr/zf_user/jobs/list/job-category?page=1");

            // 채용 공고 리스트 가져오기
            List<WebElement> jobElements = driver.findElements(By.cssSelector("ul.list_product li.item"));

            for (WebElement jobElement : jobElements) {
                // Job title 추출
                String title = jobElement.findElement(By.cssSelector("strong.tit")).getText();

                // Company 추출
                String company = jobElement.findElement(By.cssSelector("span.corp")).getText();

                // Location 추출
                String location = jobElement.findElement(By.cssSelector("li.company_local")).getText();

                // Closing date 추출
                String closingDate = jobElement.findElement(By.cssSelector("span.date")).getText();

                // URL 추출
                String jobUrl = jobElement.findElement(By.cssSelector("a")).getAttribute("href");
                jobUrl = jobUrl; // 상대 경로를 절대 경로로 변환

                // 데이터를 저장하기 위한 JobPosting 엔터티 생성
                JobPosting jobPosting = new JobPosting();
                jobPosting.setTitle(title);
                jobPosting.setCompany(company);
                jobPosting.setLocation(location);
                jobPosting.setClosingDate(closingDate);
                jobPosting.setUrl(jobUrl);

                // 중복 데이터 확인 후 저장
                if (!jobPostingRepository.existsByUrl(jobUrl)) {
                    jobPostingRepository.save(jobPosting);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }
    }
}
