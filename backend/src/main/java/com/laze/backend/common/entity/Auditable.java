package com.laze.backend.common.entity;

import java.time.LocalDateTime;

public interface Auditable {

    // 등록 정보 Setter
    void setRegEmpId(String regEmpId);
    void setRegEmpNm(String regEmpNm);
    void setRegDtm(LocalDateTime regDtm);

    // 변경 정보 Setter
    void setChgEmpId(String chgEmpId);
    void setChgEmpNm(String chgEmpNm);
    void setChgDtm(LocalDateTime chgDtm);
}
