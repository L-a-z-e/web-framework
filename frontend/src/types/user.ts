export interface UserInfo {
  empId: string;
  empNm: string;
  cmpCd: string;
  deptCd: string;
  authorities: Array<{ authority: string }>;
}
