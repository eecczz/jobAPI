package com.example.jobapi.dto;

import lombok.Data;

@Data
public class JobPostingDto {
    private Long id;
    private String title;
    private String company;
    private String location;
}
