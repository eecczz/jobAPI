package com.example.jobapi.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POSTING_ID")
    private Long id;
    private String title;
    private String company;
    private String location;
    private String closingDate;
    private String url;
}
