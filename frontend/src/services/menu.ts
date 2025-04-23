import apiClient, {getApi} from './api';
import type { MenuInfo } from '@/types/menu';
import type { ApiResponse } from "@/types/api.ts";
import axios from "axios";

/**
 * 메뉴 목록 조회 API 호출하고, 결과 데이터를 ApiResponse<MenuInfo[]> 형태로 가공하여 반환합니다.
 * @returns Promise<ApiResponse<MenuInfo[]>>
 * @throws 에러 발생 시 Axios 에러 또는 커스텀 에러
 */
export const fetchMyMenusApi = async (): Promise<ApiResponse<MenuInfo[]>> => { // <<< 반환 타입을 ApiResponse 로 변경!
  try {
    return await getApi<MenuInfo[]>('/api/menus');
  } catch (error: any) {
    console.error('API call failed in fetchMyMenusApi:', error);
    if (axios.isAxiosError(error) && error.response?.data) {
      // 서버가 실패 ApiResponse 를 보냈을 경우, 그 정보를 담아 에러 throw
      const errorData = error.response.data as Partial<ApiResponse<unknown>>;
      throw new Error(errorData.message || '메뉴 API 호출 실패 (서버 응답 오류)');
      // 또는 실패 ApiResponse 객체를 반환할 수도 있음 (호출하는 쪽에서 처리 방식 통일 필요)
      // return { success: false, code: errorData.code || 'UNKNOWN_ERROR', message: errorData.message || '메뉴 API 호출 실패', data: null };
    }
    // 네트워크 오류 등 다른 에러
    throw new Error(error.message || '메뉴 API 호출 중 네트워크 오류 발생');
  }
};
