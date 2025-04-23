package com.laze.backend.menu.controller;

import com.laze.backend.menu.dto.MenuDto;
import com.laze.backend.menu.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@Tag(name = "Menu API", description = "메뉴 조회 API")
@RestController
@RequestMapping("/api/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "내 메뉴 목록 조회", description = "현재 로그인된 사용자가 접근 가능한 메뉴 목록을 트리 구조로 조회")
    @GetMapping
    @PreAuthorize("isAuthenticated()") // 최소한 인증된 사용자만 호출 가능하도록 설정
    public List<MenuDto> getMyMenus() {
        return menuService.getUserMenus();
    }
}
