export interface UserInfo {
  empId: string;
  empNm: string;
  cmpCd: string;
  deptCd: string;
  authorities: Array<{ authority: string }>; // 권한 정보 타입 확인 필요
  // ... 기타 필요한 필드 (userId 등)
}
