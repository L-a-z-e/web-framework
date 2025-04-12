package com.laze.backend.menu.service.impl;

import com.laze.backend.menu.dto.MenuDto;
import com.laze.backend.menu.mapper.MenuMapper;
import com.laze.backend.menu.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {

    private final MenuMapper menuMapper;

    @Override
    @Transactional(readOnly = true)
    public List<MenuDto> getUserMenus() {
        // 1. 현재 사용자의 권한 정보 가져오기
        Collection<String> authorities = getCurrentUserAuthorities();
        log.debug("Current user authorities: {}", authorities);

        // 2. DB에서 접근 가능한 모든 메뉴 목록 조회 (평탄화된 리스트)
        List<MenuDto> allAccessibleMenus = menuMapper.findAccessibleMenus(authorities);
        log.debug("Found {} accessible menus from DB.", allAccessibleMenus.size());

        // 3. 평탄화된 메뉴 리스트를 트리 구조로 변환
        List<MenuDto> menuTree = buildMenuTree(allAccessibleMenus);

        return menuTree;
    }

    /**
     * SecurityContextHolder 에서 현재 사용자의 권한 목록(문자열)을 추출합니다.
     * @return 권한 문자열 Set
     */
    private Collection<String> getCurrentUserAuthorities() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return Collections.emptySet(); // 인증되지 않았으면 빈 목록 반환
        }

        // CustomUserDetails 사용 가정
        // Object principal = authentication.getPrincipal();
        // if (principal instanceof CustomUserDetails) {
        //     CustomUserDetails userDetails = (CustomUserDetails) principal;
        //     return userDetails.getAuthorities().stream()
        //             .map(GrantedAuthority::getAuthority)
        //             .collect(Collectors.toSet());
        // }

        // 간단하게 Authentication 객체의 권한 사용
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

        // return Collections.emptySet(); // 기본값
    }

    /**
     * 평탄화된 메뉴 목록을 트리 구조로 변환합니다.
     * @param flatMenuList DB에서 조회된 정렬된 메뉴 목록
     * @return 트리 구조의 메뉴 목록 (최상위 메뉴 리스트)
     */
    private List<MenuDto> buildMenuTree(List<MenuDto> flatMenuList) {
        // 메뉴 ID를 키로 하는 Map 생성 (빠른 조회용)
        Map<String, MenuDto> menuMap = flatMenuList.stream()
            .collect(Collectors.toMap(MenuDto::getMenuId, menu -> menu));

        // 트리 구조를 담을 리스트 (최상위 메뉴만 담김)
        List<MenuDto> menuTree = new ArrayList<>();

        for (MenuDto menu : flatMenuList) {
            // 하위 메뉴 리스트 초기화
            if (menu.getChildren() == null) {
                menu.setChildren(new ArrayList<>());
            }

            String upperMenuId = menu.getUpperMenuId();

            if (upperMenuId == null || upperMenuId.isEmpty()) {
                // 상위 메뉴 ID가 없으면 최상위 메뉴이므로 트리에 바로 추가
                menuTree.add(menu);
            } else {
                // 상위 메뉴 ID가 있으면 Map에서 상위 메뉴를 찾아 children 에 추가
                MenuDto parentMenu = menuMap.get(upperMenuId);
                if (parentMenu != null) {
                    // 상위 메뉴의 children 리스트가 null 일 경우 초기화
                    if (parentMenu.getChildren() == null) {
                        parentMenu.setChildren(new ArrayList<>());
                    }
                    parentMenu.getChildren().add(menu);
                } else {
                    // 상위 메뉴를 찾을 수 없는 경우 (데이터 오류 등), 로그 남기고 최상위로 취급하거나 다른 처리
                    log.warn("Parent menu not found for menuId: {}, upperMenuId: {}. Adding as root.", menu.getMenuId(), upperMenuId);
                    menuTree.add(menu);
                }
            }
        }
        log.debug("Built menu tree with {} root nodes.", menuTree.size());
        return menuTree;
    }
}
