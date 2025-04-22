package com.laze.backend.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 비밀번호 인코더 설정 클래스
 * 애플리케이션에서 사용할 PasswordEncoder 구현체를 빈으로 등록합니다.
 */
@Configuration
public class PasswordEncoderConfig {
    /**
     * BCrypt 알고리즘을 사용하는 PasswordEncoder 빈을 생성합니다.
     * 이 인코더는 사용자 비밀번호 해싱 및 검증에 사용됩니다.
     *
     * BCryptPasswordEncoder는 다음과 같은 특징이 있습니다:
     * - 단방향 해싱 알고리즘으로 원본 비밀번호를 복호화할 수 없음
     * - 솔트(salt)가 자동으로 생성되어 저장됨
     * - 기본적으로 10라운드의 해싱 작업을 수행 (보안 강도)
     * - 매번 다른 해시 값을 생성하지만, matches() 메서드로 검증 가능
     *
     * @return BCryptPasswordEncoder 인스턴스
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
