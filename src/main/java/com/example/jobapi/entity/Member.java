package com.example.jobapi.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String name;

    // 회원의 관심등록 리스트와 지원 리스트를 위한 매핑
    @OneToMany(mappedBy = "member")
    @JsonManagedReference
    private List<SaveList> saveLists;

    @OneToMany(mappedBy = "member")
    @JsonManagedReference
    private List<AppList> appLists;
}
