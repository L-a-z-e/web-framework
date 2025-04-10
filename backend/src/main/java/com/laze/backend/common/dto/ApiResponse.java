package com.laze.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자 접근 제한
@AllArgsConstructor(access = AccessLevel.PRIVATE) // 모든 필드 생성자 접근 제한
@JsonInclude(JsonInclude.Include.NON_NULL) // 응답 시 null 인 필드는 제외 (data 필드 등)
public class ApiResponse<T> {

    private boolean success;
    private String code;
    private String message;
    private T data;

    // 성공 응답 (데이터 포함)
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "OK", "Request successful", data);
    }

    // 성공 응답 (데이터 없음)
    public static ApiResponse<Void> ok() {
        return new ApiResponse<>(true, "OK", "Request successful", null);
    }

    // 실패 응답 (정의된 에러 코드 사용)
    public static ApiResponse<Void> fail(String errorCode, String errorMessage) {
        return new ApiResponse<>(false, errorCode, errorMessage, null);
    }

    // 실패 응답 (예외 객체 사용 - 개발/디버깅용, 실제 운영에서는 상세 메시지 노출 주의)
    public static ApiResponse<Void> fail(String errorCode, Exception e) {
        return new ApiResponse<>(false, errorCode, e.getMessage(), null);
    }

    // 실패 응답 (에러 코드 없이 메시지만)
    public static ApiResponse<Void> fail(String errorMessage) {
        // 일반 실패 코드를 사용하거나 정의 필요
        return new ApiResponse<>(false, "ERROR", errorMessage, null);
    }
}
