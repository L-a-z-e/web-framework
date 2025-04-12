// src/types/api.ts (새 파일 또는 common.ts 등에 추가)

/**
 * 백엔드 표준 API 응답 구조 인터페이스
 * @template T 응답 데이터의 타입
 */
export interface ApiResponse<T> {
  success: boolean; // 성공 여부
  code: string;     // 결과 코드 (성공: "OK", 실패: ErrorCode)
  message: string;  // 결과 메시지
  data?: T | null;   // 실제 응답 데이터
}

/**
 * 표준 페이징 응답 구조 인터페이스
 * (백엔드의 PageResponseDto 와 동일하게 정의)
 * @template T 페이지 컨텐츠의 타입
 */
export interface PageResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number; // 현재 페이지 번호 (0부터 시작)
  first: boolean;
  last: boolean;
  numberOfElements: number; // 현재 페이지 요소 개수
}
