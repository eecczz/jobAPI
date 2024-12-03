package com.example.jobapi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 통합 에러 처리: 모든 예외를 처리하는 글로벌 핸들러
     * @param ex - 발생한 예외 객체
     * @param request - HttpServletRequest 객체
     * @return ResponseEntity<Map<String, Object>> - 통일된 에러 응답 형식
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception ex, HttpServletRequest request) {
        // 로그 기록
        logger.error("Unhandled exception occurred at {}: ", request.getRequestURI(), ex);

        // 통일된 에러 응답 포맷
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("error", "Internal Server Error");
        errorResponse.put("message", ex.getMessage() != null ? ex.getMessage() : "Unexpected error occurred");
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * 커스텀 예외 처리
     * @param ex - 발생한 커스텀 예외 객체
     * @param request - HttpServletRequest 객체
     * @return ResponseEntity<Map<String, Object>> - 통일된 에러 응답 형식
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Map<String, Object>> handleCustomException(CustomException ex, HttpServletRequest request) {
        // 로그 기록
        logger.error("Custom exception occurred at {}: {}, ErrorCode: {}",
                request.getRequestURI(), ex.getMessage(), ex.getErrorCode(), ex);

        // 통일된 에러 응답 포맷
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", ex.getStatus().value());
        errorResponse.put("error", ex.getErrorCode());
        errorResponse.put("message", ex.getMessage());
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity.status(ex.getStatus()).body(errorResponse);
    }
}
