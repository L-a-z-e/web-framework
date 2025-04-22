package com.laze.backend.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laze.backend.common.dto.ApiResponse;
import com.laze.backend.security.dto.CustomUserDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class MenuAccessControlFilter extends OncePerRequestFilter {

    // /api/메뉴ID/... 형식의 URL 패턴 (예: /api/FW0001/list)
    private final Pattern menuIdPattern = Pattern.compile("/api/([A-Z0-9]+)(?:/.*)?");
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        log.debug("Checking menu access for URI: {}", requestURI);

        // 메뉴 ID 패턴에 해당하는 요청인지 확인
        Matcher matcher = menuIdPattern.matcher(requestURI);
        if (matcher.find()) {
            String menuId = matcher.group(1);
            log.debug("Extracted menuId: {}", menuId);

            // 현재 인증 정보 가져오기
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // 1. 인증 정보 유효성 및 타입 체크
            if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
                // 미인증 사용자 (익명 포함)
                log.warn("[MenuAccessControlFilter] Unauthenticated access attempt to menu: {}", menuId);
                // 401 Unauthorized 응답 전송
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "인증이 필요합니다.");
                return; // 필터 체인 중단
            }

            // 2. Principal 타입 체크
            if (!(authentication.getPrincipal() instanceof CustomUserDetails)) {
                // 인증은 되었으나 Principal 타입이 예상과 다른 경우 (설정 오류 등)
                log.error("[MenuAccessControlFilter] Authenticated user principal is not CustomUserDetails: {}", authentication.getPrincipal().getClass());
                // 403 Forbidden 또는 500 Internal Server Error 응답 고려
                sendErrorResponse(response, HttpStatus.FORBIDDEN, "INVALID_USER_TYPE", "사용자 정보 처리 중 오류가 발생했습니다.");
                return; // 필터 체인 중단
            }

            // 3. CustomUserDetails 로 캐스팅 및 메뉴 접근 권한 확인
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Set<String> accessibleMenuIds = userDetails.getAccessibleMenuIds(); // 로그인 시 조회된 접근 가능 메뉴 목록

            if (accessibleMenuIds == null || !accessibleMenuIds.contains(menuId)) {
                // 메뉴 접근 권한 없음
                log.warn("[MenuAccessControlFilter] Access denied to menu: {} for user: {}", menuId, userDetails.getUsername());
                // 403 Forbidden 응답 전송
                sendErrorResponse(response, HttpStatus.FORBIDDEN, "MENU_ACCESS_DENIED", "메뉴 접근 권한이 없습니다.");
                return; // 필터 체인 중단
            }

            // 4. 접근 권한 확인 통과
            log.debug("[MenuAccessControlFilter] Menu access granted for menuId: {} to user: {}", menuId, userDetails.getUsername());
            // 다음 필터로 진행 (아래 filterChain.doFilter 호출)

        } else {
            // URL 패턴에 맞지 않는 요청 (예: /api/user/me)
            log.trace("[MenuAccessControlFilter] URI does not match menu pattern, skipping menu access control: {}", requestURI);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // /api/user/와 같은 특정 경로는 필터 적용에서 제외
        return path.startsWith("/api/user/") ||
            path.equals("/api/csrf") ||
            path.equals("/api/menus") ||
            !path.startsWith("/api/");
    }

    /**
     * 공통 에러 응답 전송 헬퍼 메소드
     * @param response HttpServletResponse 객체
     * @param status   HTTP 상태 코드
     * @param code     에러 코드 문자열
     * @param message  에러 메시지 문자열
     * @throws IOException 응답 쓰기 오류 시
     */
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String code, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiResponse<Void> errorResponse = ApiResponse.fail(code, message);
        // 주입받은 objectMapper 사용
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
