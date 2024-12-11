package com.example.jobapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class JobPosting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String company;
    private String location;
    private String experience;
    private String education;
    private String employmentType;
    private String sector;
    private String salary;
    private String url;
    private String closingDate; // 마감일 필드 추가

    @ManyToOne
    private Member author; // 작성자 정보 추가

    private Long view = 0L;
    private Boolean cancel = true;

    // 필요한 생성자 추가
    public JobPosting(Boolean cancel, Member author, Long view, String closingDate, String company, String deadline,
                      String education, String employmentType, String experience, String location, String sector,
                      String salary, String title, String url) {
        this.cancel = cancel;
        this.author = author;
        this.view = view;
        this.closingDate = closingDate;
        this.company = company;
        this.education = education;
        this.employmentType = employmentType;
        this.experience = experience;
        this.location = location;
        this.sector = sector;
        this.salary = salary;
        this.title = title;
        this.url = url;
    }
}
