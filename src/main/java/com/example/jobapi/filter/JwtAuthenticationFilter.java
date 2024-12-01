package com.example.jobapi.filter;

import com.example.jobapi.util.JWTUtil;
import com.example.jobapi.util.JwtToken;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class JwtAuthenticationFilter extends org.springframework.web.filter.OncePerRequestFilter {

    private final JWTUtil jwtUtil;

    public JwtAuthenticationFilter(JWTUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String token = resolveToken(request);

        if (token != null && jwtUtil.validateToken(token)) {
            // JWT 토큰이 유효한 경우 사용자 정보 추출
            String username = jwtUtil.extractUsername(token);
            request.setAttribute("jwtToken", new JwtToken(username, token));
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 실제 토큰 부분
        }
        return null;
    }
}
