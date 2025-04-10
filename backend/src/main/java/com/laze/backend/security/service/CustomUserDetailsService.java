package com.laze.backend.security.service;

import com.laze.backend.security.dto.CustomUserDetails;
import com.laze.backend.security.dto.UserAuthInfo;
import com.laze.backend.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user by username: {}", username);

        UserAuthInfo userAuthInfo = userMapper.findUserAuthByUsername(username)
            .orElseThrow(() -> {
                log.warn("User not found with username: {}", username);
                return new UsernameNotFoundException("User not found with username: " + username);
            });

        userAuthInfo.processStatusFields();

        List<String> authorityStrings = userMapper.findAuthoritiesByUserId(userAuthInfo.getEmpId());
        List<GrantedAuthority> authorities = mapToGrantedAuthorities(authorityStrings);

        return CustomUserDetails.builder()
            .empId(userAuthInfo.getEmpId())
            .empNm(userAuthInfo.getEmpNm())
            .password(userAuthInfo.getPwno()) // DB의 BCrypt 해시값
            .enabled(userAuthInfo.isEnabled())
            .authorities(authorities)
            .accountNonLocked(!userAuthInfo.isAccountLocked())
            .credentialsNonExpired(!userAuthInfo.isCredentialsExpired())
            .accountNonExpired(true)
            // 추가 정보
            .build();
    }

    private List<GrantedAuthority> mapToGrantedAuthorities(List<String> authorities) {
        return authorities.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }
}
