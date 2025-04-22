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
    }

}
