package com.laze.backend.menu.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MenuDto {
    private String menuId;
    private String menuNm;
    private String bizDvcd;
    private String hrnMenuId;
    private Integer menuLev;
    private Integer menuOrd;
    private String scrId;
    private String menuMrkYn;
    private String menuIcon;
    private String useYn;
    private List<MenuDto> children;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
