package com.laze.backend.menu.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuDto {
    private String menuId;
    private String menuNm;
    private String hrnMenuId;
    private Integer menuOrdr;
    private Integer menuLvl;
    private String menuIcon;
    private List<MenuDto> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
