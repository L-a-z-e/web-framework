import api from './api';
import type { AxiosResponse } from 'axios';

interface LoginCredentials {
  cmpCd?: string;
  empId?: string;
  password?: string;
}

// 로그인 API 호출 함수
// Spring Security Form Login 은 application/x-www-form-urlencoded 를 기대함
export const loginApi = (credentials: LoginCredentials): Promise<AxiosResponse> => {
  // 데이터를 URLSearchParams 또는 FormData 로 변환 필요
  const params = new URLSearchParams();
  if (credentials.cmpCd) params.append('cmpCd', credentials.cmpCd);
  if (credentials.empId) params.append('empId', credentials.empId); // usernameParameter 와 일치
  if (credentials.password) params.append('password', credentials.password);

  return api.post('/api/user/login', params, { // POST 요청 및 데이터 전달
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded' // Content-Type 변경
    },
    maxRedirects: 0,
    validateStatus: function (status) {
      return status >= 200 && status < 400; // 2xx, 3xx 모두 성공으로 처리
    }
  });
};

// 로그아웃 API 호출 함수
export const logoutApi = (): Promise<AxiosResponse> => {
  return api.post('/api/user/logout');// CSRF 활성화 시 토큰 필요할 수 있음
};

// 현재 사용자 정보 조회 API 호출 함수
export const getMeApi = (): Promise<AxiosResponse> => {
  return api.get('e');
};
