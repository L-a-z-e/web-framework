import axios, { type AxiosInstance, type InternalAxiosRequestConfig, AxiosHeaders } from 'axios';

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
  (response) => {
    // console.log('Response Received', response);
    // 응답 데이터가 ApiResponse 형태일 경우 추가 처리 가능
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


// 생성된 Axios 인스턴스 내보내기
export default api;
