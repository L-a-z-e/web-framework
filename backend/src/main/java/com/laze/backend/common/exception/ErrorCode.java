package com.laze.backend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- 공통 에러 ---
    INVALID_INPUT_VALUE("COMMON-001", "입력 값이 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    METHOD_NOT_ALLOWED("COMMON-002", "허용되지 않은 요청 메소드입니다.", HttpStatus.METHOD_NOT_ALLOWED),
    RESOURCE_NOT_FOUND("COMMON-003", "요청한 리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    ACCESS_DENIED("COMMON-004", "접근 권한이 없습니다.", HttpStatus.FORBIDDEN),

    // --- 인증/인가 관련 에러 ---
    AUTHENTICATION_FAILED("AUTH-001", "인증에 실패하였습니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_FAILED("AUTH-002", "로그인 정보가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),

    // --- 사용자 관련 에러 ---
    USER_NOT_FOUND("USER-001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    DUPLICATE_USERNAME("USER-002", "이미 존재하는 사용자 아이디입니다.", HttpStatus.CONFLICT),

    // --- 데이터 관련 에러 ---
    DATA_INTEGRITY_VIOLATION("DATA-001", "데이터 무결성 제약 조건에 위배됩니다.", HttpStatus.CONFLICT),

    // --- 서버 내부 에러 ---
    INTERNAL_SERVER_ERROR("SERVER-001", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    EXTERNAL_API_ERROR("SERVER-002", "외부 API 호출 중 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE);

    private final String code;      // 에러 코드 (클라이언트에게 전달될 수 있음)
    private final String message;   // 에러 메시지
    private final HttpStatus status;  // HTTP 상태 코드
}
