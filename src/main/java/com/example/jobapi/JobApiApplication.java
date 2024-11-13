package com.example.jobapi;

import com.example.jobapi.Service.SaraminCrawlingService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class JobApiApplication {

    public static void main(String[] args) {
        // SaraminCrawlingService를 직접 호출하여 테스트
        ApplicationContext context = SpringApplication.run(JobApiApplication.class, args);
        SaraminCrawlingService crawlingService = context.getBean(SaraminCrawlingService.class);
        crawlingService.crawlSaramin(); // 수동 실행 테스트
    }

}
