package com.laze.backend.security.provider;

import com.laze.backend.security.dto.CustomUserDetails;
import com.laze.backend.security.dto.UserInfo;
import com.laze.backend.security.token.CmpUserAuthenticationToken;
import com.laze.backend.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class CmpUserAuthenticationProvider implements AuthenticationProvider {

    private final UserMapper userMapper; // DB 조회를 위해 Mapper 직접 주입
    private final PasswordEncoder passwordEncoder; // 비밀번호 비교를 위해 Encoder 주입

    // Todo application.yml 설정 필요
    @Value("${security.account.lockout-threshold:5}")
    private int lockoutThreshold;
    @Value("${security.password.expiration-days:90}")
    private long passwordExpirationDays;

    /**
     * 인증
     */
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        if (!(authentication instanceof CmpUserAuthenticationToken)) {
            return null;
        }

        CmpUserAuthenticationToken token = (CmpUserAuthenticationToken) authentication;
        String cmpCd = token.getCmpCd();
        String empId = token.getName(); // principal (empId)
        String presentedPassword = (String) token.getCredentials(); // password

        log.debug("Attempting authentication for cmpCd: {}, empId: {}", cmpCd, empId);

        try {
            // 1. UserMapper를 사용하여 cmpCd와 empId로 DB에서 사용자 정보 조회
            UserInfo userInfo = userMapper.findUserInfoByCmpCdAndEmpId(cmpCd, empId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with cmpCd: " + cmpCd + ", empId: " + empId));

            // 2. 비밀번호 검증
            if (!passwordEncoder.matches(presentedPassword, userInfo.getPwno())) {
                log.warn("Authentication failed for empId: {}. Bad credentials.", empId);
                userMapper.incrementPasswordFailureCount(cmpCd, empId);
                throw new BadCredentialsException("Invalid username or password");
            }

            // 3. UserDetails 상태 필드 계산
            boolean enabled = !"Y".equalsIgnoreCase(userInfo.getRetmYn());
            boolean accountNonLocked = userInfo.getPwnoErrorRtrv() == null || userInfo.getPwnoErrorRtrv() < lockoutThreshold;
            boolean credentialsNonExpired = checkPasswordNotExpired(userInfo.getPwnoChgDt());
            boolean accountNonExpired = true;

            // 4. 계정 상태 검증
            if (!enabled) {
                throw new DisabledException("User account is disabled(retired)");
            }
            if (!accountNonLocked) {
                throw new LockedException("User account is locked");
            }
            if (!accountNonExpired) {
                throw new BadCredentialsException("User account has expired");
            }
            if (!credentialsNonExpired) {
                throw new BadCredentialsException("User credentials have expired");
            }

            // 5. 권한 정보 조회 및 변환
            List<GrantedAuthority> authorities = mapToGrantedAuthorities(userInfo.getAuthorityGroupIds());

            // 6. 접근 가능 메뉴 ID 목록 조회
            Set<String> accessibleMenuIds = userInfo.getAccessibleMenuIds() != null ? userInfo.getAccessibleMenuIds() : Collections.emptySet();

            // 7. 인증 성공! CustomUserDetails 객체 생성
            CustomUserDetails userDetails = buildCustomUserDetails(userInfo, authorities, enabled, accountNonExpired, accountNonLocked, credentialsNonExpired, accessibleMenuIds);

            log.info("Authentication successful for empId: {}", empId);

            // 8. 인증된 CmpUserAuthenticationToken 반환 (비밀번호는 null 처리)
            return new CmpUserAuthenticationToken(cmpCd, userDetails, null, userDetails.getAuthorities());

        } catch (UsernameNotFoundException | BadCredentialsException | DisabledException | LockedException e) {
            // 로그에 예외 종류 기록
            log.warn("Authentication failed for empId: {}. Reason: {}", empId, e.getMessage());
            throw e; // Spring Security가 처리
        } catch (Exception e) {
            log.error("Unexpected error during authentication for empId: {}", empId, e);
            // 세부 정보 노출 방지
            throw new BadCredentialsException("Authentication failed due to an unexpected error");
        }
    }

    /**
     * supports 지정
     */
    @Override
    public boolean supports(Class<?> authentication) {
        return CmpUserAuthenticationToken.class.isAssignableFrom(authentication);
    }


    // --- 헬퍼 메소드들 (CustomUserDetailsService에서 가져오거나 유사하게 구현) ---

    private CustomUserDetails buildCustomUserDetails(UserInfo userInfo, Collection<? extends GrantedAuthority> authorities,
                                                     boolean enabled, boolean accountNonExpired, boolean accountNonLocked, boolean credentialsNonExpired,
                                                     Set<String> accessibleMenuIds) {
        return CustomUserDetails.builder()
            .empId(userInfo.getEmpId())
            .password(userInfo.getPwno())
            .authorities(authorities)
            .enabled(enabled)
            .accountNonExpired(accountNonExpired)
            .accountNonLocked(accountNonLocked)
            .credentialsNonExpired(credentialsNonExpired)
            .cmpCd(userInfo.getCmpCd())
            .empNm(userInfo.getEmpNm())
            .deptCd(userInfo.getDeptCd())
            .jobclsCd(userInfo.getJobclsCd())
            .jobclsNm(userInfo.getJobclsNm())
            .telNo(userInfo.getTelNo())
            .hpNo(userInfo.getHpNo())
            .mail(userInfo.getMail())
            .cmpNm(userInfo.getCmpNm())
            .bizcpRegNo(userInfo.getBizcpRegNo())
            .ceoNm(userInfo.getCeoNm())
            .rprsTelNo(userInfo.getRprsTelNo())
            .postNo(userInfo.getPostNo())
            .addr(userInfo.getAddr())
            .deptNm(userInfo.getDeptNm())
            .hqCd(userInfo.getHqCd())
            .hqNm(userInfo.getHqNm())
            .dpldEmpId(userInfo.getDpldEmpId())
            .accessibleMenuIds(accessibleMenuIds)
            .build();
    }

    private List<GrantedAuthority> mapToGrantedAuthorities(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    private boolean checkPasswordNotExpired(String changeDateStr) {
        if (changeDateStr == null || changeDateStr.length() != 8) {
            log.debug("Password change date is missing or invalid, assuming password is not expired.");
            return true;
        }
        try {
            LocalDate changeDate = LocalDate.parse(changeDateStr, DateTimeFormatter.BASIC_ISO_DATE);
            return !changeDate.isBefore(LocalDate.now().minusDays(passwordExpirationDays));
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse password change date '{}', assuming password is not expired.", changeDateStr, e);
            return true;
        }
    }
}
