package com.laze.backend.security.filter;

import com.laze.backend.security.token.CmpUserAuthenticationToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.Assert;

public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    // 로그인 폼에서 전달될 파라미터 이름 정의 (SecurityConfig와 일치 필요)
    public static final String SPRING_SECURITY_FORM_COMPANY_CODE_KEY = "cmpCd"; // 회사 코드 파라미터 이름
    public static final String SPRING_SECURITY_FORM_USERNAME_KEY = "empId";     // 직원 ID 파라미터 이름
    public static final String SPRING_SECURITY_FORM_PASSWORD_KEY = "password";  // 비밀번호 파라미터 이름 (pwno 로 변경했다면 수정)

    // RequestMatcher: 이 필터가 어떤 요청을 처리할지 결정 (로그인 URL 및 메소드)
    private static final AntPathRequestMatcher LOGIN_REQUEST_MATCHER =
        new AntPathRequestMatcher("/api/user/login", "POST"); // SecurityConfig 설정과 일치

    private String companyCodeParameter = SPRING_SECURITY_FORM_COMPANY_CODE_KEY;
    private boolean postOnly = true;

    // 생성자: AuthenticationManager를 주입받고, 로그인 처리 URL 설정
    public CustomAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager); // 부모 생성자에 AuthenticationManager 전달
        // 부모 클래스의 setRequiresAuthenticationRequestMatcher를 호출하여 URL/메소드 설정
        setRequiresAuthenticationRequestMatcher(LOGIN_REQUEST_MATCHER);
        // 파라미터 이름 설정 (부모 클래스의 필드 설정 메소드 호출)
        setUsernameParameter(SPRING_SECURITY_FORM_USERNAME_KEY);
        setPasswordParameter(SPRING_SECURITY_FORM_PASSWORD_KEY);
    }

    /**
     * 실제 인증 시도 로직을 오버라이드
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        if (this.postOnly && !request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        // HttpServletRequest에서 cmpCd, empId, password 파라미터 값 추출
        String cmpCd = obtainCompanyCode(request);
        String username = obtainUsername(request); // 부모 클래스의 obtainUsername 사용 (empId 파라미터)
        String password = obtainPassword(request); // 부모 클래스의 obtainPassword 사용 (password 파라미터)

        // null 값 처리 (기본적으로 빈 문자열로 처리, 필요시 예외 발생 등 커스텀 가능)
        cmpCd = (cmpCd != null) ? cmpCd.trim() : "";
        username = (username != null) ? username.trim() : "";
        password = (password != null) ? password : "";

        // 우리가 만든 CmpUserAuthenticationToken 객체 생성 (인증 전 상태)
        CmpUserAuthenticationToken authRequest = new CmpUserAuthenticationToken(cmpCd, username, password);

        // 요청 상세 정보 설정 (IP 주소, 세션 ID 등) - 부모 클래스 기능 활용
        setDetails(request, authRequest);

        // AuthenticationManager에게 실제 인증 처리 위임
        // AuthenticationManager는 CmpUserAuthenticationProvider를 찾아 authenticate 메소드 호출
        return this.getAuthenticationManager().authenticate(authRequest);
    }

    /**
     * 요청에서 회사 코드 파라미터 값을 가져오는 메소드
     * @param request HttpServletRequest
     * @return 회사 코드 값 (없으면 null)
     */
    protected String obtainCompanyCode(HttpServletRequest request) {
        return request.getParameter(this.companyCodeParameter);
    }

    /**
     * (선택적) 회사 코드 파라미터 이름을 변경할 수 있도록 Setter 추가
     * @param companyCodeParameter HTML 폼에서 사용할 회사 코드 input의 name 속성값
     */
    public void setCompanyCodeParameter(String companyCodeParameter) {
        Assert.hasText(companyCodeParameter, "Company code parameter must not be empty or null");
        this.companyCodeParameter = companyCodeParameter;
    }

    /**
     * @param postOnly true면 POST만 허용
     */
    @Override
    public void setPostOnly(boolean postOnly) {
        this.postOnly = postOnly;
    }

}
