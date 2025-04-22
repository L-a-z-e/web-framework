package com.laze.backend.user.controller;

import com.laze.backend.common.annotation.CurrentUser;
import com.laze.backend.common.dto.ApiResponse;
import com.laze.backend.common.exception.BusinessException;
import com.laze.backend.common.exception.ErrorCode;
import com.laze.backend.security.dto.CustomUserDetails;
import com.laze.backend.security.dto.UserInfo;
import com.laze.backend.user.mapper.UserMapStructMapper;
import com.laze.backend.user.mapper.UserMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException; // 필요시 추가
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/user")
public class UserController {

    private final UserMapper userMapper;
    private final UserMapStructMapper userMapStructMapper;
    private final CsrfTokenRepository csrfTokenRepository;

    /**
     * 현재 사용자 정보 출력
     * @param customUserDetails
     * @return
     */
    @GetMapping("/me")
    public ApiResponse<UserInfo> getCurrentUserInfo(@CurrentUser CustomUserDetails customUserDetails) {
        if (customUserDetails == null) {
            throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED);
        }

        UserInfo userInfo = userMapStructMapper.toUserInfo(customUserDetails);
        return ApiResponse.ok(userInfo);
    }

    /**
     * CSRF Token 발급
     * @param request HTTP 요청
     * @param response HTTP 응답
     * @return CSRF 토큰 객체
     */
    @GetMapping("/csrf")
    public CsrfToken csrf(HttpServletRequest request, HttpServletResponse response) {
        // 현재 요청과 관련된 CSRF 토큰을 로드하거나 생성
        CsrfToken token = csrfTokenRepository.loadToken(request);
        if (token == null) {
            token = csrfTokenRepository.generateToken(request);
            csrfTokenRepository.saveToken(token, request, response); // 응답에 쿠키 저장
        }
        log.info("CSRF token requested: {}", token.getToken());
        return token; // 토큰 정보 반환 (헤더 이름, 파라미터 이름, 토큰 값 포함)
    }

    @PostMapping("/test")
    public ApiResponse<Void> testapi(){
        return ApiResponse.ok();
    }
}
