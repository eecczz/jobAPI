package com.example.jobapi.advice;

import jakarta.servlet.http.HttpSession;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addLoginStatusToModel(HttpSession session, Model model) {
        // 세션에서 로그인 정보 가져오기
        String username = (String) session.getAttribute("username");
        Boolean loggedIn = (Boolean) session.getAttribute("loggedIn");

        // 모델에 로그인 상태 추가
        model.addAttribute("username", username != null ? username : "anonymousUser");
        model.addAttribute("loggedIn", loggedIn != null ? loggedIn : false);
    }
}
