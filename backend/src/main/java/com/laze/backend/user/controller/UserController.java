package com.laze.backend.user.controller;

import com.laze.backend.security.dto.CustomUserDetails;
import org.springframework.security.access.AccessDeniedException; // 필요시 추가
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/user")
public class UserController {

    @GetMapping("/me")
    // --- !!! 반환 타입을 CustomUserDetails 로 변경 !!! ---
    public CustomUserDetails getCurrentUser(
        @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails != null) {
            // --- !!! 성공 시 UserDetails 객체 직접 반환 !!! ---
            // ApiResponseWrapper 가 자동으로 ApiResponse.ok() 로 래핑해 줌
            return userDetails;
        } else {
            // 인증되지 않은 사용자가 이 API 에 접근하면 Spring Security 가 먼저 차단함 (401 또는 로그인 리다이렉션).
            // 만약 어떤 이유로 인증 후에 userDetails 가 null 이 되는 비정상 상황이라면 예외 발생 고려.
            // 여기서는 null 을 반환하면 Wrapper 가 data: null 인 응답 생성 가능 (또는 예외 발생)
            // throw new BusinessException(ErrorCode.AUTHENTICATION_FAILED); // 예: 인증 실패 예외 발생
            // 또는 접근 거부 예외 발생
            throw new AccessDeniedException("인증 정보가 유효하지 않습니다.");
        }
    }
}
