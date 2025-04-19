package com.laze.backend.security.token;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class CmpUserAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final String cmpCd;

    public CmpUserAuthenticationToken(String cmpCd, Object principal, Object credentials) {
        super(principal, credentials);
        this.cmpCd = cmpCd;
        setAuthenticated(false);
    }

    public CmpUserAuthenticationToken(String cmpCd, Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(principal, credentials, authorities);
        this.cmpCd = cmpCd;
//        setAuthenticated(true); ✅ 인증 위치확인 -> 인증이 완료된(trusted) Authentication 객체를 만들 때는 반드시 생성자를 통해서만 authenticated 상태를 true로 설정하도록 강제
    }

}
