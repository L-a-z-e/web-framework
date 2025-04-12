package com.laze.backend.menu.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuDto {
    private String menuId; // 라우터 경로 역할
    private String menuNm;
    private String upperMenuId;
    private Integer menuOrdr;
    private Integer menuLvl;
    private String menuIcon;
    private List<MenuDto> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
