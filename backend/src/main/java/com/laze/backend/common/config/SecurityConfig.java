package com.laze.backend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laze.backend.common.dto.ApiResponse;
import com.laze.backend.security.filter.CustomAuthenticationFilter;
import com.laze.backend.security.filter.MenuAccessControlFilter;
import com.laze.backend.security.handler.CustomAuthenticationSuccessHandler;
import com.laze.backend.security.provider.CmpUserAuthenticationProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@Slf4j
@RequiredArgsConstructor
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource; // CORS 설정 Bean
    private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler; // 로그인 성공 핸들러
    private final CmpUserAuthenticationProvider cmpUserAuthenticationProvider; // 커스텀 Provider Bean 주입
    private final AuthenticationConfiguration authenticationConfiguration; // AuthenticationManager 얻기 위함
    private final ObjectMapper objectMapper; // <<< ObjectMapper 주입 추가

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource, MenuAccessControlFilter menuAccessControlFilter) throws Exception {

        // CsrfTokenRequestAttributeHandler 생성 (Spring Security 6.x에서 권장)
        CsrfTokenRequestAttributeHandler requestHandler = new CsrfTokenRequestAttributeHandler();
        // 토큰을 "_csrf" 요청 속성으로 저장
        requestHandler.setCsrfRequestAttributeName("_csrf");

        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository()) // Bean 사용 변경
                .csrfTokenRequestHandler(requestHandler)
                .ignoringRequestMatchers(
                    new AntPathRequestMatcher("/api/user/login", "POST"),
                    new AntPathRequestMatcher("/api/user/csrf", "GET"),
                    new AntPathRequestMatcher("/api/user/logout", "POST")
                )
            )
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/error", "/api/user/login", "/api/user/csrf").permitAll()
                .requestMatchers("/api/menus").authenticated()
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated() // 나머지 인증 필요
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authenticationEntryPoint()) // 401 Unauthorized 처리
                .accessDeniedHandler(accessDeniedHandler())       // 403 Forbidden 처리
            )
            .addFilterAt(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(menuAccessControlFilter, AuthorizationFilter.class)
            .logout(logout -> logout
                .logoutUrl("/api/user/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    ApiResponse<Void> apiResponse = ApiResponse.ok();
                    objectMapper.writeValue(response.getWriter(), apiResponse);
                })
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
            )
            .sessionManagement(session -> session
                .sessionFixation().changeSessionId()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login?expired=true")
                .maximumSessions(1) // 동일 사용자는 하나의 세션만 허용
            )
            .authenticationProvider(cmpUserAuthenticationProvider);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 커스텀 인증 필터 Bean 생성 메소드
    @Bean
    public CustomAuthenticationFilter customAuthenticationFilter() throws Exception {
        // AuthenticationManager를 주입받아 필터 생성
        CustomAuthenticationFilter filter = new CustomAuthenticationFilter(authenticationManager());

        // 필터에 로그인 성공 핸들러 설정
        filter.setAuthenticationSuccessHandler(customAuthenticationSuccessHandler);

        filter.setAuthenticationFailureHandler((request, response, exception) -> {
            log.warn("Login failed for user '{}': {}", request.getParameter(CustomAuthenticationFilter.SPRING_SECURITY_FORM_USERNAME_KEY), exception.getMessage());

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            // 실패 원인에 따른 에러 코드 및 메시지 설정
            String errorCode = "LOGIN_FAILED"; // 기본 실패 코드
            String errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다."; // 기본 메시지

            // 특정 예외 타입에 따라 메시지 구체화
            if (exception instanceof LockedException) {
                errorCode = "ACCOUNT_LOCKED";
                errorMessage = "계정이 잠겼습니다.";
            } else if (exception instanceof DisabledException) {
                errorCode = "ACCOUNT_DISABLED";
                errorMessage = "비활성화된 계정입니다.";
            } else if (exception instanceof UsernameNotFoundException) {
                errorCode = "USER_NOT_FOUND";
                errorMessage = "존재하지 않는 사용자입니다.";
            }

            ApiResponse<Void> failResponse = ApiResponse.fail(errorCode, errorMessage);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(response.getWriter(), failResponse);
            } catch (IOException e) {
                log.error("Error writing JSON error response", e);
            }
        });

        return filter;

    }

    // --- 인증/인가 실패 시 JSON 응답 처리를 위한 핸들러 Bean 추가 ---
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        // 401 Unauthorized 에러 발생 시 호출될 핸들러
        return (request, response, authException) -> {
            log.warn("Authentication required: {}", authException.getMessage());
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "인증이 필요합니다.", authException);
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        // 403 Forbidden 에러 발생 시 호출될 핸들러
        return (request, response, accessDeniedException) -> {
            log.warn("Access denied: {}", accessDeniedException.getMessage());
            log.debug("Request details - method: {}, remote addr: {}, user: {}",
                request.getMethod(), request.getRemoteAddr(),
                SecurityContextHolder.getContext().getAuthentication().getName());
            sendErrorResponse(response, HttpStatus.FORBIDDEN, "ACCESS_DENIED", "접근 권한이 없습니다.", accessDeniedException);
        };
    }

    // --- 공통 에러 응답 전송 메소드 ---
    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String code, String message, Exception exception) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        ApiResponse<Void> errorResponse = ApiResponse.fail(code, message);
        // 주입받은 objectMapper 사용
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:9000")); //
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        CookieCsrfTokenRepository tokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        tokenRepository.setCookieName("XSRF-TOKEN");
        tokenRepository.setHeaderName("X-XSRF-TOKEN");
        tokenRepository.setCookiePath("/");
        return tokenRepository;
    }


}
