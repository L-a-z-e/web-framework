import axios, {type AxiosInstance, type InternalAxiosRequestConfig, AxiosHeaders, type AxiosResponse} from 'axios';
import type {ApiResponse} from "@/types/api.ts";

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

api.defaults.xsrfCookieName = 'XSRF-TOKEN';
api.defaults.xsrfHeaderName = 'X-XSRF-TOKEN';

// CSRF 토큰 초기화 함수
export async function initializeCsrf(): Promise<void> {
  try {
    console.log('CSRF 토큰 초기화 시작...');
    const response = await api.get('/api/user/csrf');
    console.log('CSRF 토큰 초기화 완료:', response.data);

    // CSRF 토큰 확인 (디버깅 용도)
    const cookies = document.cookie.split(';').map(c => c.trim());
    const csrfCookie = cookies.find(c => c.startsWith('XSRF-TOKEN='));
    console.log('현재 CSRF 쿠키:', csrfCookie);

    return Promise.resolve();
  } catch (error) {
    console.error('CSRF 토큰 초기화 실패:', error);
    return Promise.reject(error);
  }
}

/**
 * ApiResponse로 래핑된 데이터를 처리하는 GET 요청 메서드
 * 타입 T는 response.data.data(실제 데이터)의 타입입니다.
 *
 * @param url API 엔드포인트 URL
 * @param config 추가 Axios 설정 (선택적)
 * @returns ApiResponse<T> 타입의 Promise (success, code, message, data 포함)
 */
export function getApi<T = any>(url: string, config?: any): Promise<ApiResponse<T>> {
  // ApiResponse<T> 타입으로 응답을 명시하여 호출
  return api.get<ApiResponse<T>>(url, config).then(res => res.data);
}

/**
 * ApiResponse로 래핑된 데이터를 처리하는 POST 요청 메서드
 * 타입 T는 response.data.data(실제 데이터)의 타입입니다.
 * 타입 D는 요청 본문(request body)의 타입입니다.
 *
 * @param url API 엔드포인트 URL
 * @param data 요청 본문 데이터 (선택적)
 * @param config 추가 Axios 설정 (선택적)
 * @returns ApiResponse<T> 타입의 Promise (success, code, message, data 포함)
 */
export function postApi<T = any, D = any>(url: string, data?: D, config?: any): Promise<ApiResponse<T>> {
  return api.post<ApiResponse<T>>(url, data, config).then(res => res.data);
}

/**
 * ApiResponse로 래핑된 데이터를 처리하는 PUT 요청 메서드
 * 타입 T는 response.data.data(실제 데이터)의 타입입니다.
 * 타입 D는 요청 본문(request body)의 타입입니다.
 *
 * @param url API 엔드포인트 URL
 * @param data 요청 본문 데이터 (선택적)
 * @param config 추가 Axios 설정 (선택적)
 * @returns ApiResponse<T> 타입의 Promise (success, code, message, data 포함)
 */
export function putApi<T = any, D = any>(url: string, data?: D, config?: any): Promise<ApiResponse<T>> {
  return api.put<ApiResponse<T>>(url, data, config).then(res => res.data);
}

/**
 * ApiResponse로 래핑된 데이터를 처리하는 DELETE 요청 메서드
 * 타입 T는 response.data.data(실제 데이터)의 타입입니다.
 *
 * @param url API 엔드포인트 URL
 * @param config 추가 Axios 설정 (선택적)
 * @returns ApiResponse<T> 타입의 Promise (success, code, message, data 포함)
 */
export function deleteApi<T = any>(url: string, config?: any): Promise<ApiResponse<T>> {
  return api.delete<ApiResponse<T>>(url, config).then(res => res.data);
}

/**
 * ApiResponse로 래핑된 데이터를 처리하는 PATCH 요청 메서드
 * 타입 T는 response.data.data(실제 데이터)의 타입입니다.
 * 타입 D는 요청 본문(request body)의 타입입니다.
 *
 * @param url API 엔드포인트 URL
 * @param data 요청 본문 데이터 (선택적)
 * @param config 추가 Axios 설정 (선택적)
 * @returns ApiResponse<T> 타입의 Promise (success, code, message, data 포함)
 */
export function patchApi<T = any, D = any>(url: string, data?: D, config?: any): Promise<ApiResponse<T>> {
  return api.patch<ApiResponse<T>>(url, data, config).then(res => res.data);
}


// --- 요청 인터셉터 (Optional)---
// 모든 요청 전에 특정 작업을 수행 (예: 인증 토큰 추가 - JWT 사용 시)
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 요청 정보 로깅 (디버깅 용도)
    console.log(`요청: ${config.method?.toUpperCase()} ${config.url}`);

    // CSRF 토큰 수동 추가 (안전성 강화)
    const csrfToken = getCsrfTokenFromCookie();
    if (csrfToken && config.method && ['post', 'put', 'delete', 'patch'].includes(config.method.toLowerCase())) {
      if (!config.headers) config.headers = new AxiosHeaders();
      config.headers.set('X-XSRF-TOKEN', csrfToken);
      console.log('CSRF 토큰이 요청에 추가됨:', csrfToken.substring(0, 10) + '...');
    }

    console.log('Request:', config.method, config.url);
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
  <T = any>(response: AxiosResponse<ApiResponse<T> | any>) => { // <<< 응답 타입 명시
    const responseData = response.data;

    // --- ApiResponse 구조인지 타입 가드를 사용하여 확인 ---
    if (isApiResponse(responseData)) { // <<< 타입 가드 함수 사용
      if (responseData.success) {
        // 성공 시, response.data 를 실제 데이터(T 타입)로 교체
        console.log('API Success:', responseData.code, responseData.message);

        return response;
      } else {
        // API 레벨 실패(success: false) 시 에러 처리
        console.error('API Error:', responseData.code, responseData.message);
        // 에러 객체 생성 (Error 또는 커스텀 에러 클래스)
        const apiError = new Error(responseData.message || 'API request failed');
        // 추가 정보 포함
        (apiError as any).response = response; // 원본 응답 포함
        (apiError as any).code = responseData.code; // 에러 코드 포함
        (apiError as any).apiData = responseData; // 전체 ApiResponse 포함 (선택적)
        return Promise.reject(apiError);
      }
    }
    // -------------------------------------------------

    // ApiResponse 구조가 아니면 (파일 다운로드 등) 그대로 반환
    // console.warn('Response data is not in ApiResponse format:', responseData);
    return response; // <<< 타입은 AxiosResponse<any> 유지
  },
  (error) => {
    // 응답 오류 처리 (네트워크 오류, 서버 오류 등)
    console.error('Response Error:', error);

    // CSRF 오류 감지 및 자동 복구 시도
    if (error.response && error.response.status === 403) {
      // error.response.data가 문자열인지 확인
      const isCSRFError = typeof error.response.data === 'string' &&
        error.response.data.includes('CSRF');

      // JSON 객체인 경우 message 필드 확인
      const hasCsrfMessage = error.response.data &&
        error.response.data.message &&
        typeof error.response.data.message === 'string' &&
        error.response.data.message.includes('CSRF');

      if (isCSRFError || hasCsrfMessage) {
        console.warn('CSRF 오류 감지됨, 토큰 재설정 시도...');

        // 원래 요청 저장
        const originalRequest = error.config;

        // 이미 재시도한 요청은 다시 재시도하지 않음
        if (originalRequest._retry) {
          return Promise.reject(error);
        }

        originalRequest._retry = true;

        // 토큰 재설정 후 원래 요청 재시도
        return initializeCsrf()
          .then(() => {
            // CSRF 토큰 수동으로 다시 설정
            const token = getCsrfTokenFromCookie();
            if (token) {
              originalRequest.headers['X-XSRF-TOKEN'] = token;
            }
            console.log('CSRF 토큰 재설정 후 요청 재시도');
            return api(originalRequest);
          })
          .catch(retryError => {
            console.error('CSRF 토큰 재설정 실패, 요청 실패:', retryError);
            return Promise.reject(error); // 원래 오류 반환
          });
      }
    }


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

// 쿠키에서 CSRF 토큰 가져오기
function getCsrfTokenFromCookie(): string | null {
  const cookies = document.cookie.split(';');
  for (const cookie of cookies) {
    const trimmedCookie = cookie.trim();
    if (trimmedCookie.startsWith('XSRF-TOKEN=')) {
      return decodeURIComponent(trimmedCookie.substring('XSRF-TOKEN='.length));
    }
  }
  return null;
}

// --- !!! ApiResponse 타입 가드 함수 추가 !!! ---
/**
 * 주어진 데이터가 ApiResponse<any> 형태인지 확인하는 타입 가드 함수
 * @param data 확인할 데이터
 * @returns ApiResponse<any> 형태이면 true, 아니면 false
 */
function isApiResponse<T = any>(data: any): data is ApiResponse<T> {
  // data 가 null 이 아니고 객체이며, success, code, message 필드를 가지고 있는지 확인
  // data 필드는 optional 이므로 체크 제외 가능 또는 'data' in data 로 확인
  return data !== null && typeof data === 'object' &&
    typeof data.success === 'boolean' &&
    typeof data.code === 'string' &&
    typeof data.message === 'string';
  // && 'data' in data; // data 필드 존재 여부까지 확인하려면 추가
}

// 생성된 Axios 인스턴스 내보내기
export default api;
