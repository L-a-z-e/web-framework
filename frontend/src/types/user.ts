export interface UserInfo {
  cmpCd: string;
  empId: string;
  empNm: string;
  deptCd: string;
  jobclsCd?: string;
  jobclsNm?: string;
  telNo?: string;
  hpNo?: string;
  mail?: string;
  cmpNm?: string;
  bizcpRegNo?: string;
  ceoNm?: string;
  rprsTelNo?: string;
  postNo?: string;
  addr?: string;
  deptNm?: string;
  hqCd?: string;
  hqNm?: string;
  dpldEmpId?: string;
  authorities: string[];
  accessibleMenuIds?: Set<string>;
}
