package com.example.jobapi.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SignupRequest {

    private String username; // 사용자 이름
    private String password; // 비밀번호

    // 기본 생성자
    public SignupRequest() {
    }

    // 필드 생성자
    public SignupRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getter와 Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
