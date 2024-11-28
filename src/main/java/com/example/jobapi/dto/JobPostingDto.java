package com.example.jobapi.dto;

import lombok.Data;

@Data
public class JobPostingDto {
    private Long id;
    private String title;          // 채용 제목
    private String company;        // 회사명
    private String location;       // 지역
    private String closingDate;    // 마감일
    private String url;            // 채용공고 URL

    private String salary;         // 급여 정보
    private String sector;
    private String experience;     // 경력 요구사항
    private String employmentType; // 고용 형태 (정규직, 계약직 등)
    private String education;      // 학력 요구사항
}
