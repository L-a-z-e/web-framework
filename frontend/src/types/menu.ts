export interface MenuInfo {
  menuId: string;                 // MENU_ID
  menuNm: string;                 // MENU_NM
  bizDvcd: string;                // BIZ_DVCD
  hrnMenuId: string | null;       // HRN_MENU_ID
  menuLev: number;                // MENU_LEV
  menuOrd: number;                // MENU_ORD
  scrId: string | null;           // SCR_ID
  menuMrkYn: string | null;       // MENU_MRK_YN
  menuIcon: string | null;        // MENU_ICON
  useYn: string;                  // USE_YN
  rmk: string | null;             // RMK
  children?: MenuInfo[] | null;   // 트리 구조를 위한 필드 유지
}
