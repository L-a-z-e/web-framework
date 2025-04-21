package com.laze.backend.common.annotation;

import java.lang.annotation.*;

/**
 * 이 어노테이션이 적용된 Controller 메소드 또는 클래스는
 * ApiResponseWrapper 에 의한 자동 응답 래핑 대상에서 제외됩니다.
 * (예: 파일 다운로드, 특정 커스텀 응답 등)
 */
@Target({ElementType.METHOD, ElementType.TYPE}) // 메소드 또는 클래스 레벨에 적용 가능
@Retention(RetentionPolicy.RUNTIME) // 런타임에도 어노테이션 정보 유지
@Documented
public @interface NoApiResponseWrapper {
}
