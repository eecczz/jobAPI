package com.example.jobapi.util;

public class JwtToken {
    private String username;
    private String token;

    public JwtToken(String username, String token) {
        this.username = username;
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public String getToken() {
        return token;
    }
}
