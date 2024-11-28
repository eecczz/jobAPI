package com.example.jobapi;

import com.example.jobapi.service.SaraminCrawlingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class JobApiApplication {

    public static void main(String[] args) {
        // Spring Boot 애플리케이션 실행
        ApplicationContext context = SpringApplication.run(JobApiApplication.class, args);

        // SaraminCrawlingService를 수동으로 실행
        SaraminCrawlingService crawlingService = context.getBean(SaraminCrawlingService.class);

        // 키워드와 페이지 수를 지정하여 테스트 실행
        String keyword = "python"; // 크롤링 키워드
        int pages = 20; // 크롤링할 페이지 수

        crawlingService.crawlSaramin(keyword, pages); // 크롤링 실행
        System.out.println("크롤링 작업이 완료되었습니다.");
    }
}
