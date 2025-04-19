package com.laze.backend.security.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

@Getter
@Builder
public class CustomUserDetails implements UserDetails {

    // --- UserDetails 필수 필드 ---
    private final String empId;
    @JsonIgnore
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;
    private final boolean enabled;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;

    // FW_EMP 테이블 정보
    private final String cmpCd;
    private final String empNm;
    private final String deptCd;
    private final String jobclsCd;
    private final String jobclsNm;
    private final String telNo;
    private final String hpNo;
    private final String mail;

    // FW_CMP 테이블 정보
    private final String cmpNm;
    private final String bizcpRegNo;
    private final String ceoNm;
    private final String rprsTelNo;
    private final String postNo;
    private final String addr;

    // FW_DEPT 테이블 정보
    private final String deptNm;
    private final String hqCd;
    private final String hqNm;
    private final String dpldEmpId;

    // 접근 가능 메뉴 정보
    private final Set<String> accessibleMenuIds; // 접근 가능한 메뉴 ID 목록

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

