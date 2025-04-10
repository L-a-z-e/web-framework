package com.laze.backend.user.mapper;

import com.laze.backend.security.dto.UserAuthInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    // 사용자 이름으로 인증/부가 정보 조회
    Optional<UserAuthInfo> findUserAuthByUsername(@Param("empId") String empId);
    // 사용자 ID로 권한 목록 조회
    List<String> findAuthoritiesByUserId(@Param("empId") String empId);
}
