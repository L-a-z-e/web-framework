package com.laze.backend.security.service;

import com.laze.backend.security.dto.CustomUserDetails;
import com.laze.backend.security.dto.UserInfo;
import com.laze.backend.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CustomUserDetailsService {
    // ☑️ CustomProvider 중복으로 삭제 고려
}
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class CustomUserDetailsService implements UserDetailsService {
//
//    private final UserMapper userMapper;
//    // --- 설정값 주입 (application.yml 등에서 관리) ---
//    @Value("${security.account.lockout-threshold:5}") // 계정 잠금 임계값 (기본값 5)
//    private int lockoutThreshold;
//
//    @Value("${security.password.expiration-days:90}") // 비밀번호 만료 기간(일) (기본값 90)
//    private long passwordExpirationDays;
//
//    @Override
//    @Transactional(readOnly = true)
//    public UserDetails loadUserByUsername(String empId) throws UsernameNotFoundException {
//        log.debug("Loading user by empId: {}", empId);
//
//        UserInfo userInfo = userMapper.findUserInfoByUsername(empId)
//            .orElseThrow(() -> {
//                log.warn("User not found with empId: {}", empId);
//                return new UsernameNotFoundException("User not found with empId: " + empId);
//            });
//
//        boolean enabled = !"Y".equalsIgnoreCase(userInfo.getRetmYn()); // 퇴사 아니면 활성
//        boolean accountNonLocked = userInfo.getPwnoErrorRtrv() == null || userInfo.getPwnoErrorRtrv() < lockoutThreshold; // 실패 횟수가 임계값 미만이면 잠기지 않음
//        boolean credentialsNonExpired = checkPasswordNotExpired(userInfo.getPwnoChgDt()); // 비밀번호 만료 여부 체크
//        boolean accountNonExpired = true; // 계정 자체 만료 여부 (여기서는 항상 true)
//
//        // 3. 권한 정보 변환 (UserAuthInfo DTO에 포함된 authorityGroupIds 사용 가정)
//        List<GrantedAuthority> authorities = mapToGrantedAuthorities(userInfo.getAuthorityGroupIds());
//
//        // 4. 접근 가능 메뉴 ID 목록 (UserAuthInfo DTO에 포함된 accessibleMenuIds 사용 가정)
//        Set<String> accessibleMenuIds = userInfo.getAccessibleMenuIds() != null ? userInfo.getAccessibleMenuIds() : Collections.emptySet(); // null 체크
//
//        // 5. 최종 CustomUserDetails 객체 생성 및 반환
//        return CustomUserDetails.builder()
//            // --- UserDetails 필수 필드 설정 ---
//            .empId(userInfo.getEmpId())
//            .password(userInfo.getPwno()) // 암호화된 비밀번호
//            .authorities(authorities)      // 변환된 권한 목록
//            .enabled(enabled)             // 계산된 값 사용
//            .accountNonExpired(accountNonExpired) // 계산된 값 사용
//            .accountNonLocked(accountNonLocked)   // 계산된 값 사용
//            .credentialsNonExpired(credentialsNonExpired) // 계산된 값 사용
//
//            // --- 추가 정보 필드 설정 (userInfo DTO에서 가져옴) ---
//            .cmpCd(userInfo.getCmpCd())
//            .empNm(userInfo.getEmpNm())
//            .deptCd(userInfo.getDeptCd())
//            .jobclsCd(userInfo.getJobclsCd())
//            .jobclsNm(userInfo.getJobclsNm())
//            .telNo(userInfo.getTelNo())
//            .hpNo(userInfo.getHpNo())
//            .mail(userInfo.getMail())
//            .cmpNm(userInfo.getCmpNm())
//            .bizcpRegNo(userInfo.getBizcpRegNo())
//            .ceoNm(userInfo.getCeoNm())
//            .rprsTelNo(userInfo.getRprsTelNo())
//            .postNo(userInfo.getPostNo())
//            .addr(userInfo.getAddr())
//            .deptNm(userInfo.getDeptNm())
//            .hqCd(userInfo.getHqCd())
//            .hqNm(userInfo.getHqNm())
//            .dpldEmpId(userInfo.getDpldEmpId())
//            .accessibleMenuIds(accessibleMenuIds) // 조회된 메뉴 ID 목록
//            .build();
//    }
//
//    // 권한 문자열 목록 -> GrantedAuthority 객체 목록 변환
//    private List<GrantedAuthority> mapToGrantedAuthorities(List<String> authorities) {
//        if (authorities == null || authorities.isEmpty()) {
//            return Collections.emptyList();
//        }
//        return authorities.stream()
//            .map(SimpleGrantedAuthority::new)
//            .collect(Collectors.toList());
//    }
//
//    // 비밀번호 만료 여부 체크
//    private boolean checkPasswordNotExpired(String changeDateStr) {
//
//        if (changeDateStr == null || changeDateStr.length() != 8) {
//            log.debug("Password change date is missing or invalid, assuming password is not expired.");
//            return true; // 날짜 정보 없으면 만료 안 됨으로 간주 (정책에 따라 false 가능)
//        }
//        try {
//            LocalDate changeDate = LocalDate.parse(changeDateStr, DateTimeFormatter.BASIC_ISO_DATE);
//            return !changeDate.isBefore(LocalDate.now().minusDays(passwordExpirationDays));
//        } catch (DateTimeParseException e) {
//            log.warn("Failed to parse password change date '{}', assuming password is not expired.", changeDateStr, e);
//            return true; // 날짜 형식 오류 시 만료 안 됨으로 간주 (정책에 따라 false 가능)
//        }
//    }
//}
