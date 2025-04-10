package com.laze.backend.security.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Builder
public class CustomUserDetails implements UserDetails {

    // --- UserDetails 필수 필드 ---
    private final String empId; // 로그인 ID (예: empId)
    private final String password; // DB에 저장된 "BCrypt로 암호화된" 비밀번호
    @Builder.Default
    private final boolean enabled = true; // 계정 활성화 여부 (DB 연동 필요)
    private final Collection<? extends GrantedAuthority> authorities; // 권한 목록

    // --- 계정 상태 필드 (DB 연동 필요) ---
    @Builder.Default
    private final boolean accountNonExpired = true;
    private final boolean accountNonLocked; // 예: 로그인 실패 횟수 기반
    @Builder.Default
    private final boolean credentialsNonExpired = true; // 예: 비밀번호 변경 주기 기반

    // --- 애플리케이션에서 사용할 추가 사용자 정보 ---
    private final String empNm; // 사용자 이름 (empNm)
    private final String cmpCd; // 회사 코드
    private final String deptCd; // 부서 코드
    private final String jobclsCd;
    private final String jobclsNm;
    private final String telNo;
    private final String hpNo;
    private final String mail;
    private final String deptDvcd;
    private final String dpldEmpId;
    private final String dpldEmpNm;
    private final String loginDate;
    private final String loginTime;
    private final String bfLoginDate;
    private final String bfLoginTime;
    private final String sessionId;
    private final String ipLocal;
    private final String ipServer;
    private final String guid;
    private final String yyyymm;
    private final String loginDvcd;
    private final String pwChgTerm;
    // ... 기타 필요한 필드 ...

    // --- !!! passwordSalt 필드 제거 !!! ---

    // UserDetails 인터페이스 메소드 구현 (Lombok @Getter 로 대부분 자동 생성됨)
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.authorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.empId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return this.accountNonExpired;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return this.credentialsNonExpired;
    }

    @Override
    public boolean isEnabled() {
        return this.enabled;
    }
}

