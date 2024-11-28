package com.example.jobapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchCondition {
    private String title;
    private String keyword;
    private String company;
    private String position;
    private String location;
    private String experience;
    private String salary;
    private String sortOrder;
}
