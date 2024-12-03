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

        if (token != null) {
            // 사용자 이름 추출
            String username = jwtUtil.extractUsername(token);

            // JWT 토큰 유효성 확인
            if (username != null && jwtUtil.validateToken(token, username)) {
                // 유효한 토큰인 경우 JwtToken 객체를 생성하여 요청 속성에 추가
                request.setAttribute("jwtToken", new JwtToken(username, token));
            }
        }

        // 필터 체인 계속 진행
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
