package com.laze.backend.common.controller;

import com.laze.backend.common.aop.NoApiResponseWrapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/public")
public class PublicController {
    @GetMapping("/init")
    @NoApiResponseWrapper
    public void init(HttpServletRequest request, HttpServletResponse response) {
        // CSRF 토큰 생성
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        if (token != null) {
            // 쿠키에 CSRF 토큰 설정
            Cookie cookie = new Cookie("XSRF-TOKEN", token.getToken());
            cookie.setHttpOnly(false);  // 클라이언트에서 읽을 수 있도록
            cookie.setPath("/");  // 전체 경로에서 유효하도록 설정
            cookie.setMaxAge(60 * 60); // 1시간 유효

            // 응답에 쿠키 추가
            response.addCookie(cookie);
        }
        // 응답은 별도의 본문 없이 완료
    }
}
