package com.laze.backend.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laze.backend.common.dto.ApiResponse;
import com.laze.backend.security.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    /**
     * 로그인 성공 시 호출되는 메소드.
     * 리다이렉션 대신 200 OK 와 함께 사용자 정보를 JSON 으로 응답
     * @param request 요청 객체
     * @param response 응답 객체
     * @param authentication 성공한 인증 정보 (내부에 UserDetails 포함)
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        // 세션 확인 및 로깅
        HttpSession session = request.getSession(false);
        String sessionId = session != null ? session.getId() : "No Session";
        log.info("Login successful. Authentication Principal: {}, Session ID: {}",
            authentication.getName(), sessionId);

        // 세션이 없으면 생성 (명시적으로)
        if (session == null) {
            session = request.getSession(true);
            log.info("Created new session with ID: {}", session.getId());
        }

        // SecurityContext를 세션에 명시적으로 저장 (핵심 수정 부분)
        SecurityContext securityContext = SecurityContextHolder.getContext();
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
        log.info("SecurityContext saved to session. Authentication: {}", authentication.getName());

        log.info("Login successful. Authentication Principal: {}", authentication.getName());

        Object principal = authentication.getPrincipal();

        if (principal instanceof CustomUserDetails) {
            CustomUserDetails userDetails = (CustomUserDetails) principal;

            // 성공 응답 생성 (ApiResponse 사용)
            ApiResponse<CustomUserDetails> successResponse = ApiResponse.ok(userDetails);

            // HTTP 상태 코드 설정 (200 OK)
            response.setStatus(HttpStatus.OK.value());
            // Content Type 설정 (JSON)
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            // 응답 본문에 JSON 쓰기
            try {
                objectMapper.writeValue(response.getWriter(), successResponse);
            } catch (IOException e) {
                log.error("Error writing JSON response", e);
                throw e;
            }
        } else {
            log.error("Unexpected principal type: {}", principal.getClass());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            // 간단한 에러 메시지 응답 또는 GlobalExceptionHandler 로 위임 고려
            response.getWriter().write("{\"success\":false, \"code\":\"AUTH_ERROR\", \"message\":\"Invalid user details type after authentication.\"}");
        }
    }
}
