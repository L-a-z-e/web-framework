package com.laze.backend.security.provider;

import com.laze.backend.security.dto.CustomUserDetails;
import com.laze.backend.security.dto.UserInfo;
import com.laze.backend.security.token.CmpUserAuthenticationToken;
import com.laze.backend.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmpUserAuthenticationProviderTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CmpUserAuthenticationProvider authProvider;

    private UserInfo userInfo;
    private CmpUserAuthenticationToken token;
    private final String cmpCd = "AD1000";
    private final String empId = "user";
    private final String password = "$2a$10$JqYPnA5Wx0MAveZ0LWfl9OmNtrIuPBcCtmyQXri4E3zaxLtV1/NZO";

    @BeforeEach
    void setUp() {
        // 필드에 값 직접 설정 (@Value가 테스트에서 동작하지 않으므로)
        ReflectionTestUtils.setField(authProvider, "lockoutThreshold", 5);
        ReflectionTestUtils.setField(authProvider, "passwordExpirationDays", 90L);

        // 테스트용 사용자 정보 준비
        userInfo = new UserInfo();
        userInfo.setEmpId(empId);
        userInfo.setCmpCd(cmpCd);
        userInfo.setPwno(password);
        userInfo.setEmpNm("테스트 사용자");
        userInfo.setRetmYn("N"); // 은퇴하지 않음
        userInfo.setPwnoErrorRtrv(0); // 로그인 실패 횟수
        userInfo.setPwnoChgDt(LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)); // 오늘 비밀번호 변경
        userInfo.setDeptCd("DEPT001");
        userInfo.setDeptNm("개발부서");
        userInfo.setJobclsCd("JOB001");
        userInfo.setJobclsNm("개발자");
        userInfo.setCmpNm("테스트 회사");
        userInfo.setAuthorityGroupIds(new ArrayList<>(Collections.singletonList("ROLE_USER")));
        userInfo.setAccessibleMenuIds(new HashSet<>(Collections.singletonList("MENU001")));

        // 테스트용 인증 토큰 준비
        token = new CmpUserAuthenticationToken(cmpCd, empId, password);
    }

    @Test
    @DisplayName("인증 성공: 유효한 사용자 정보로 인증 시도")
    void authenticate_WithValidCredentials_ShouldReturnAuthenticatedToken() {
        // Given
        when(userMapper.findUserInfoByCmpCdAndEmpId(cmpCd, empId))
            .thenReturn(Optional.of(userInfo));
        when(passwordEncoder.matches(password, password)).thenReturn(true);

        // When
        Authentication result = authProvider.authenticate(token);

        // Then
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        verify(userMapper, never()).incrementPasswordFailureCount(anyString(), anyString());
    }

    @Test
    @DisplayName("인증 실패: 존재하지 않는 사용자로 인증 시도")
    void authenticate_WithNonExistentUser_ShouldThrowUsernameNotFoundException() {
        // Given
        when(userMapper.findUserInfoByCmpCdAndEmpId(cmpCd, empId))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            authProvider.authenticate(token);
        });
    }

    @Test
    @DisplayName("인증 실패: 잘못된 비밀번호로 인증 시도")
    void authenticate_WithInvalidPassword_ShouldThrowBadCredentialsException() {
        // Given
        when(userMapper.findUserInfoByCmpCdAndEmpId(cmpCd, empId))
            .thenReturn(Optional.of(userInfo));
        when(passwordEncoder.matches(password, password)).thenReturn(false);

        // When & Then
        assertThrows(BadCredentialsException.class, () -> {
            authProvider.authenticate(token);
        });
        verify(userMapper).incrementPasswordFailureCount(cmpCd, empId);
    }

    @Test
    @DisplayName("supports 메소드: CmpUserAuthenticationToken 클래스 지원 여부 확인")
    void supports_WithCmpUserAuthenticationToken_ShouldReturnTrue() {
        // When
        boolean result = authProvider.supports(CmpUserAuthenticationToken.class);

        // Then
        assertTrue(result);
    }

    @Test
    @DisplayName("supports 메소드: 다른 인증 토큰 클래스 지원 여부 확인")
    void supports_WithOtherAuthenticationToken_ShouldReturnFalse() {
        // When
        boolean result = authProvider.supports(Authentication.class);

        // Then
        assertFalse(result);
    }
}
