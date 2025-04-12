package com.laze.backend.menu.mapper;
import com.laze.backend.menu.dto.MenuDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection; // 권한 목록 타입 (Set 또는 List)
import java.util.List;

@Mapper
public interface MenuMapper {

    /**
     * 사용자가 접근 가능한 모든 메뉴 목록을 조회
     * USE_YN='Y'
     * 권한(authorities)이 없거나 사용자가 가진 권한과 하나와 일치하는 메뉴만 조회
     * 메뉴 레벨과 순서로 정렬하여 반환
     *
     * @param authorities 사용자가 가진 권한(Role 또는 Authority) 문자열 컬렉션
     * @return 접근 가능한 메뉴 DTO 목록
     */
    List<MenuDto> findAccessibleMenus(@Param("authorities") Collection<String> authorities);

}
