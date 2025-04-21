package com.laze.backend.common.resolver;

import com.laze.backend.common.annotation.CurrentUser;
import com.laze.backend.security.dto.CustomUserDetails;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {
    /**
     * 이 Resolver 가 특정 파라미터를 지원하는지 여부를 반환합니다.
     * @param parameter 검사할 메소드 파라미터
     * @return 지원하면 true, 아니면 false
     */
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        // 파라미터에 @CurrentUser 어노테이션이 있고, 파라미터 타입이 CustomUserDetails 클래스인지 확인
        return parameter.hasParameterAnnotation(CurrentUser.class)
            && CustomUserDetails.class.isAssignableFrom(parameter.getParameterType());
    }

    /**
     * 실제 파라미터 값을 해석하여 반환합니다.
     * @param parameter          메소드 파라미터 정보
     * @param mavContainer       ModelAndView 컨테이너 (일반적으로 사용 X)
     * @param webRequest         현재 웹 요청 정보
     * @param binderFactory      WebDataBinder 팩토리 (일반적으로 사용 X)
     * @return 해석된 파라미터 값 (여기서는 CustomUserDetails 객체 또는 null)
     * @throws Exception 해석 중 발생 가능한 예외
     */
    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 정보가 있고, principal 이 CustomUserDetails 타입이면 해당 객체를 반환
        if (authentication != null && authentication.isAuthenticated()
            && authentication.getPrincipal() instanceof CustomUserDetails) {
            return authentication.getPrincipal();
        }

        // 그 외 경우 (인증되지 않았거나, principal 타입이 다르거나 등) null 반환
        // 또는 필요에 따라 예외를 던질 수도 있음 (예: @CurrentUser 사용했는데 인증 안 된 경우)
        return null;
    }
}
