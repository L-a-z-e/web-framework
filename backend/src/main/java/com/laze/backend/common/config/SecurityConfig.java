package com.laze.backend.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laze.backend.common.dto.ApiResponse;
import com.laze.backend.security.filter.CustomAuthenticationFilter;
import com.laze.backend.security.handler.CustomAuthenticationSuccessHandler;
import com.laze.backend.security.provider.CmpUserAuthenticationProvider;
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
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
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
    // private final CustomUserDetailsService customUserDetailsService; // Provider가 직접 DB 조회 시 불필요
    private final CmpUserAuthenticationProvider cmpUserAuthenticationProvider; // 커스텀 Provider Bean 주입 (@Component 가정)
    private final AuthenticationConfiguration authenticationConfiguration; // AuthenticationManager 얻기 위함

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {

//        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
//        csrfTokenRepository.setHeaderName("X-XSRF-TOKEN");
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
//            .csrf(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
//            .csrf(csrf -> csrf
//                .csrfTokenRepository(csrfTokenRepository)
//                .ignoringRequestMatchers(new AntPathRequestMatcher("/api/login", "POST")))
//                .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/login", "/error", "/api/public/**", "/api/user/csrf").permitAll()
                .requestMatchers("/api/admin/**").hasAuthority("ADMIN")
                .anyRequest().authenticated() // 나머지 인증 필요
            )
            .addFilterAt(customAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
//            .formLogin(form -> form
//                .loginPage("/login")
//                .loginProcessingUrl("/api/login")
//                .usernameParameter("empId")
//                .passwordParameter("password")
//                .successHandler(customAuthenticationSuccessHandler)
//                .failureUrl("/login?error=true")
//                .permitAll()
//            )
            .logout(logout -> logout
                .logoutUrl("/api/user/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionFixation().changeSessionId()
                .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
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

            response.setStatus(HttpStatus.UNAUTHORIZED.value()); // 401 상태 코드
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());

            // 실패 원인에 따른 에러 코드 및 메시지 설정
            String errorCode = "LOGIN_FAILED"; // 기본 실패 코드
            String errorMessage = "아이디 또는 비밀번호가 올바르지 않습니다."; // 기본 메시지

            // 특정 예외 타입에 따라 메시지 구체화 (선택적)
            if (exception instanceof LockedException) {
                errorCode = "ACCOUNT_LOCKED";
                errorMessage = "계정이 잠겼습니다.";
            } else if (exception instanceof DisabledException) {
                errorCode = "ACCOUNT_DISABLED";
                errorMessage = "비활성화된 계정입니다.";
            } else if (exception instanceof UsernameNotFoundException) {
                // UsernameNotFound는 보통 BadCredentials로 변환되지만, 직접 잡는다면
                errorMessage = "존재하지 않는 사용자입니다.";
            } // ... 기타 필요한 예외 처리 ...

            // ApiResponse 사용 (또는 직접 JSON 문자열 생성)
            ApiResponse<Void> failResponse = ApiResponse.fail(errorCode, errorMessage);
            try {
                // ObjectMapper 인스턴스 생성 또는 주입 필요
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.writeValue(response.getWriter(), failResponse);
            } catch (IOException e) {
                log.error("Error writing JSON error response", e);
            }
        });
        // ---------------------------------------------

        return filter;

        // 필터에 로그인 실패 핸들러 설정 (기본: 지정된 URL로 리다이렉트)
//        filter.setAuthenticationFailureHandler((request, response, exception) -> {
//            log.warn("Login failed: {}", exception.getMessage()); // 실패 로그 추가
//            String failureUrl = "/login?error=true"; // 실패 시 이동할 URL
//            // request.getContextPath() 를 추가하여 Context Path가 있는 경우에도 올바르게 동작하도록 함
//            response.sendRedirect(request.getContextPath() + failureUrl);
//        });

        // (Optional) 파라미터 이름 변경 시 여기서 Setter 호출 가능
        // filter.setCompanyCodeParameter("companyCode");
        // filter.setUsernameParameter("employeeId");
        // filter.setPasswordParameter("userPassword");

//        return filter;
    }

//
//    @Bean
//    public AuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
//        provider.setUserDetailsService(customUserDetailsService);
//        provider.setPasswordEncoder(passwordEncoder());
//        return provider;
//    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // --- !!! 허용 출처를 9000 포트로 변경 !!! ---
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:9000")); //
        // -----------------------------------------
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
