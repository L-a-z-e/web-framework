export interface MenuInfo {
  menuId: string; // 라우터 경로 또는 그룹 식별자
  menuNm: string;
  upperMenuId: string | null;
  menuOrdr: number;
  menuLvl: number;
  menuIcon?: string | null;
  jobDvcd: string;
  authCd: string;
  useYn: string;
  rmk?: string | null;
  children?: MenuInfo[] | null;
}
