package com.example.jobapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
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
    private String deadline;
    private String sector;
    private String salary;
    private String url;
    private String closingDate; // 마감일 필드 추가

    @ManyToOne
    private Member author; // 작성자 정보 추가
    private Long view = 0L;
    private Boolean cancel = true;
}
