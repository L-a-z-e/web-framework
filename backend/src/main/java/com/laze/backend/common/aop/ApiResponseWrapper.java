package com.laze.backend.common.aop;
import com.laze.backend.common.dto.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource; // 파일 다운로드 등 Resource 타입 확인
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail; // Spring 6+ 표준 오류 응답
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody; // 스트리밍 응답 확인

/**
 * Controller 응답 본문을 표준 ApiResponse<T> 형식으로 래핑하는 Advice.
 * .@NoApiResponseWrapper 등)에서는 래핑하지 않음.
 */
@RestControllerAdvice(basePackages = "com.laze.backend")
public class ApiResponseWrapper implements ResponseBodyAdvice<Object> {

    /**
     * 이 Advice 를 적용할지 여부를 결정
     * @param returnType Controller 메소드의 반환 타입 정보
     * @param converterType 사용될 HttpMessageConverter 타입
     * @return Advice 적용 여부 (true: 적용, false: 미적용)
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 1. 메소드 또는 클래스 레벨에 @NoApiResponseWrapper 어노테이션이 있는지 확인
        if (returnType.hasMethodAnnotation(NoApiResponseWrapper.class) ||
            returnType.getContainingClass().isAnnotationPresent(NoApiResponseWrapper.class)) {
            return false;
        }

        // 2. 반환 타입이 ResponseEntity 이면 래핑 안함 (Controller 가 직접 제어)
        if (ResponseEntity.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // 3. 파일 다운로드 등 Resource 타입은 래핑 안함
        if (Resource.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        // 4. 스트리밍 응답 타입은 래핑 안함
        if (StreamingResponseBody.class.isAssignableFrom(returnType.getParameterType())) {
            return false;
        }

        return true;
    }

    /**
     * 응답 본문을 클라이언트에 보내기 전에 가로채서 수정(래핑)합니다.
     * @param body Controller 메소드가 반환한 원본 응답 본문
     * @param returnType Controller 메소드의 반환 타입 정보
     * @param selectedContentType 최종적으로 결정된 Content-Type
     * @param selectedConverterType 사용될 HttpMessageConverter
     * @param request 현재 요청 객체
     * @param response 현재 응답 객체
     * @return 수정된 (래핑된) 응답 본문 또는 원본 본문
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // Controller 에서 이미 ApiResponse 로 감싸서 반환했거나,
        // GlobalExceptionHandler 에서 이미 ApiResponse 로 실패 응답을 만든 경우,
        // 또는 표준 오류 응답 객체인 경우 그대로 반환 (중복 래핑 방지)
        if (body instanceof ApiResponse || body instanceof ProblemDetail) {
            return body;
        }

        // --- 성공적인 경우, 원본 body 를 ApiResponse.ok() 로 래핑 ---
        // Controller 메소드가 void 를 반환하거나 null 을 반환하는 경우도 고려 가능
        if (body == null && returnType.getParameterType().equals(Void.TYPE)) {
            // Controller 메소드 반환 타입이 void 인 경우 (예: @ResponseStatus(HttpStatus.NO_CONTENT))
            // 또는 명시적으로 null을 반환하며 성공 처리를 원하는 경우
            return ApiResponse.ok(); // 데이터 없는 성공 응답
        } else {
            // 일반적인 성공 데이터가 있는 경우
            return ApiResponse.ok(body); // 원본 데이터를 data 필드에 넣어 래핑
        }
        // 참고: 만약 Controller 에서 null 을 반환했지만 실패로 처리하고 싶다면,
        // Controller 에서 직접 예외를 던지거나 ResponseEntity<ApiResponse<Void>> 를 사용해야 함.
    }
}
