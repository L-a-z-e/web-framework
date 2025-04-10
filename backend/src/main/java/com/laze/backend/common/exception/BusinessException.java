package com.laze.backend.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * 비즈니스 로직 처리 중 발생하는 예외를 위한 기본 클래스.
 * 에러 코드, 메시지, HTTP 상태 코드를 포함할 수 있음.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode; // String 대신 ErrorCode Enum 사용

    /**
     * ErrorCode Enum 을 받아 BusinessException 생성
     * @param errorCode 미리 정의된 ErrorCode
     */
    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage()); // 예외 메시지는 Enum 에서 가져옴
        this.errorCode = errorCode;
    }

    /**
     * ErrorCode 와 추가적인 상세 메시지로 생성 (기존 메시지 뒤에 덧붙임)
     * @param errorCode 미리 정의된 ErrorCode
     * @param detailMessage 추가 상세 메시지
     */
    public BusinessException(ErrorCode errorCode, String detailMessage) {
        super(errorCode.getMessage() + " (" + detailMessage + ")"); // 상세 메시지 추가
        this.errorCode = errorCode;
    }

    public String getCode() {
        return errorCode.getCode();
    }

    public HttpStatus getHttpStatus() {
        return errorCode.getStatus();
    }
}
