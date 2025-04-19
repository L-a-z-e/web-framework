package com.laze.backend.user.mapper;

import com.laze.backend.security.dto.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface UserMapper {
    // 사용자 이름으로 인증/부가 정보 조회
    Optional<UserInfo> findUserInfoByCmpCdAndEmpId(@Param("cmpCd") String cmpCd, @Param("empId") String empId);

    // 로그인 성공/실패시 오류 회수 업데이트
    void resetPasswordFailureCount(@Param("empId") String empId, @Param("cmpCd") String cmpCd);
    void incrementPasswordFailureCount(@Param("empId") String empId, @Param("cmpCd") String cmpCd);
}
