package com.laze.backend.menu.service;

import com.laze.backend.menu.dto.MenuDto;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface MenuService {
    /**
     * 현재 로그인한 사용자가 접근 가능한 메뉴 목록을 트리 구조로 조회
     * @return 메뉴 DTO 트리 리스트
     */
    List<MenuDto> getUserMenus();
}
