import axios, { type AxiosInstance, type InternalAxiosRequestConfig } from 'axios';

// 백엔드 API 기본 URL 설정 (환경 변수 사용 권장)
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:9001'; // .env 파일에 VITE_API_BASE_URL 설정 가능

console.log('API Base URL:', baseURL);

// Axios 인스턴스 생성
const api: AxiosInstance = axios.create({
  baseURL: baseURL,
  withCredentials: true,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 5000,
});

// --- 요청 인터셉터 (Optional)---
// 모든 요청 전에 특정 작업을 수행 (예: 인증 토큰 추가 - JWT 사용 시)
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // console.log('Starting Request', config);
    // JWT 토큰 사용 시
    // const token = localStorage.getItem('accessToken');
    // if (token) {
    //   config.headers.Authorization = `Bearer ${token}`;
    // }
    return config;
  },
  (error) => {
    console.error('Request Error:', error);
    return Promise.reject(error);
  }
);

// --- 응답 인터셉터 (Optional) ---
// 모든 응답 수신 후 특정 작업을 수행 (예: 전역 오류 처리, 데이터 가공)
api.interceptors.response.use(
  (response) => {
    // console.log('Response Received', response);
    // 응답 데이터가 우리가 정의한 ApiResponse 형태일 경우 추가 처리 가능
    // if (response.data && response.data.success === false) {
    //   // API 레벨에서 실패 처리 (예: 에러 메시지 표시)
    //   console.error('API Error:', response.data.code, response.data.message);
    //   // 필요시 여기서 예외를 발생시키거나 특정 상태 업데이트
    //   return Promise.reject(new Error(response.data.message || 'API request failed'));
    // }
    return response; // 성공 시 응답 객체 그대로 반환
  },
  (error) => {
    // 응답 오류 처리 (네트워크 오류, 서버 오류 등)
    console.error('Response Error:', error);
    if (error.response) {
      // 서버가 상태 코드로 응답한 경우
      console.error('Status:', error.response.status);
      console.error('Data:', error.response.data);
      // 예: 401 Unauthorized 시 로그인 페이지로 리다이렉트
      // if (error.response.status === 401) { router.push('/login'); }
    } else if (error.request) {
      // 요청은 했으나 응답을 받지 못한 경우
      console.error('No response received:', error.request);
    } else {
      // 요청 설정 중 오류 발생
      console.error('Error setting up request:', error.message);
    }
    // 오류를 계속 전파하여 각 호출 지점에서 처리하거나, 여기서 전역 처리
    return Promise.reject(error);
  }
);

// 생성된 Axios 인스턴스 내보내기
export default api;
