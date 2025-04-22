package com.laze.backend.security.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;

@Getter
@Setter
public class UserInfo {
    // --- FW_EMP 테이블 정보 ---
    private String cmpCd;
    private String empId;
    private String empNm;
    private String deptCd;
    private String jobclsCd;
    private String jobclsNm;
    private String telNo;
    private String hpNo;
    private String mail;
    private String retmYn;
    private String pwno;
    private Integer pwnoErrorRtrv;
    private String pwnoChgDt;

    // --- FW_CMP 테이블 정보 ---
    private String cmpNm;
    private String bizcpRegNo;
    private String ceoNm;
    private String rprsTelNo;
    private String postNo;
    private String addr;

    // --- FW_DEPT 테이블 정보 ---
    private String deptNm;
    private String hqCd;
    private String hqNm;
    private String dpldEmpId;

    private List<String> authorityGroupIds;
    private Set<String> accessibleMenuIds;
}
