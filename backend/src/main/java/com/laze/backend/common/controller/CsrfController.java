package com.laze.backend.common.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/deprecated")
public class CsrfController {
//
//    private final CsrfTokenRepository csrfTokenRepository;
//
//    public CsrfController() {
//        // 직접 인스턴스 생성
//        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
//        tokenRepository.setCookieName("XSRF-TOKEN");
//        tokenRepository.setHeaderName("X-XSRF-TOKEN");
//        tokenRepository.setCookiePath("/");
//        this.csrfTokenRepository = tokenRepository;
//    }
//
//
////    @Autowired
////    public CsrfController() {
////        this.csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
////        ((CookieCsrfTokenRepository)this.csrfTokenRepository).setCookieName("XSRF-TOKEN");
////        ((CookieCsrfTokenRepository)this.csrfTokenRepository).setHeaderName("X-XSRF-TOKEN");
////    }
//
//    @GetMapping
//    public CsrfToken csrf(HttpServletRequest request, HttpServletResponse response) {
//        // 강제로 새 토큰 생성 및 쿠키 저장
//        CsrfToken token = csrfTokenRepository.generateToken(request);
//        csrfTokenRepository.saveToken(token, request, response);
//        log.info("New CSRF token generated: {}", token.getToken());
//        return token;
//    }
//
////    @GetMapping("/init")
////    @NoApiResponseWrapper
////    public void init(HttpServletRequest request, HttpServletResponse response) {
////        // CSRF 토큰 생성
////        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
////        if (token != null) {
////            // 쿠키에 CSRF 토큰 설정
////            Cookie cookie = new Cookie("XSRF-TOKEN", token.getToken());
////            cookie.setHttpOnly(false);  // 클라이언트에서 읽을 수 있도록
////            cookie.setPath("/");  // 전체 경로에서 유효하도록 설정
////            cookie.setMaxAge(60 * 60); // 1시간 유효
////
////            // 응답에 쿠키 추가
////            response.addCookie(cookie);
////        }
////        // 응답은 별도의 본문 없이 완료
////    }
}
