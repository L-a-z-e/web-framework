package com.laze.backend.security.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Getter
@Setter
public class UserAuthInfo {
    private String cmpCd;
    private String empId;
    private String empNm;
    private String deptCd;
    private String deptNm;
    private String jobclsCd;
    private String jobclsNm;
    private String telNo;
    private String hpNo;
    private String mail;
    private String retmYn;
    private String pwno;
    private Integer pwnoErrorRtrv;
    private String pwnoChgDt;

    private boolean enabled;
    private boolean accountLocked;
    private boolean credentialsExpired;

    // 생성자나 별도 메소드에서 DB값 기반으로 가공 필드 설정 가능 (예시)
    public void processStatusFields() {
        this.enabled = !"Y".equalsIgnoreCase(this.retmYn); // 퇴사 여부가 'Y'가 아니면 활성화
        this.accountLocked = this.pwnoErrorRtrv != null && this.pwnoErrorRtrv >= 5; // 오류 5회 이상 시 잠금
        this.credentialsExpired = checkPasswordExpired(this.pwnoChgDt);
        this.credentialsExpired = false; // 임시: 만료되지 않음

    }

    private boolean checkPasswordExpired(String changeDateStr) {
        if (changeDateStr == null || changeDateStr.length() != 8) {
            return false; // 날짜 정보 없으면 만료 안됨으로 간주
        }
        try {
            LocalDate changeDate = LocalDate.parse(changeDateStr, DateTimeFormatter.BASIC_ISO_DATE);
            return changeDate.isBefore(LocalDate.now().minusDays(90)); // 90일 이전 변경 시 만료
        } catch (DateTimeParseException e) {
            return false; // 날짜 형식 오류 시 만료 안됨으로 간주
        }
    }

}
