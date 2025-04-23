import { ref, computed } from 'vue';
import apiClient from '@/services/api.ts';
import { defineStore } from 'pinia';
import type { UserInfo } from "@/types/user.ts";
import api from "@/services/api.ts";
import {useMenuStore, useTagsViewStore} from "@/store";
import {getMeApi, logoutApi} from "@/services/auth.ts";
import router from "@/router";

export const useAuthStore = defineStore('auth', () => {
    // === State ===
    // 로그인한 사용자 정보 (세션 유효 시 여기에 정보 저장)
    const userInfo = ref<UserInfo | null>(null);
    // 애플리케이션 로딩 상태 (선택 사항: 초기 사용자 정보 로딩 시 사용)
    const isLoading = ref<boolean>(false);

    // === Getters ===
    // 로그인 여부 (사용자 정보 객체의 존재 유무로 판단)
    const isLoggedIn = computed(() => !!userInfo.value);
    // 사용자 이름 (없으면 'Guest')
    const username = computed(() => userInfo.value?.empNm || 'Guest');
    // 사용자 ID
    const empId = computed<string | null>(() => userInfo.value?.empId || null);
    // const empId = computed<string | null>(() => { // 반환 타입 명시
    //   const user = userInfo.value;
    //   if (user && typeof user === 'object' && 'empId' in user) { // 타입 가드
    //     return user.empId;
    //   }
    //   return null;
    // });
    // 회사 코드
    const cmpCd = computed<string | null>(() => userInfo.value?.cmpCd || null);
    // 권한 목록 (GrantedAuthority 객체에서 실제 권한 문자열만 추출)
    const authorities = computed<string[]>(() => {
      return userInfo.value?.authorities || [];
    });

    function hasRole(role: string): boolean {
      const roleToCheck = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
      return authorities.value.includes(roleToCheck);
    }

    function hasAuthority(authority: string): boolean {
      return authorities.value.includes(authority);
    }

    // === Actions ===
    /**
     * 사용자 정보를 스토어에 설정합니다. (주로 로그인 성공 후 또는 앱 초기 로딩 시 호출)
     * @param user 사용자 정보 객체 또는 null
     */
    function setUserInfo(user: UserInfo | null) {
      userInfo.value = user;
      if (user) {
        console.log('User info set in store:', user.empId);
      } else {
        console.log('User info cleared from store.');
      }
    }

    /**
     * 앱 시작/새로고침 시 서버에 현재 로그인 상태 및 사용자 정보를 요청하여 스토어 초기화.
     * ApiResponseWrapper 사용을 전제로 함 (성공 시 response.data 에 UserInfo 포함)
     * @returns Promise<boolean> 로그인 상태 여부
     */
    async function checkLoginStatus(): Promise<boolean> {
      // 이미 스토어에 사용자 정보가 있거나(Persist 플러그인 복원 등), 로딩 중이면 중복 실행 방지
      if (userInfo.value || isLoading.value) {
        console.log('Skipping auth check. Already logged in or loading.');
        return isLoggedIn.value; // 현재 로그인 상태 반환
      }
      isLoading.value = true;
      try {
        // GET /api/user/me 호출 (Axios 제네릭으로 UserInfo 타입 명시)
        const response = await getMeApi();

        // --- !!! 수정된 부분: Wrapper 적용 기준으로 응답 처리 !!! ---
        // 성공 응답(200 OK)이고 응답 데이터(UserInfo)가 있으면 성공
        if (response.success && response.data) {
          setUserInfo(response.data); // 스토어에 사용자 정보 설정
          // menuStore
          const menuStore = useMenuStore();
          await menuStore.fetchMenus();

          console.log('Login status checked and user info set:', (userInfo.value as UserInfo | null)?.empId);
          return true; // 로그인 상태 true 반환
        } else {
          // API 호출은 성공했으나 데이터가 없는 비정상 상황
          console.warn('checkLoginStatus: Received successful response but no user data.');
          await logout(); // 로그아웃 처리
          return false;
        }
      } catch (error: any) {
        // API 호출 실패 (401 Unauthorized, 403 Forbidden, 네트워크 오류 등)
        // -> 세션이 없거나 만료된 것으로 간주하고 로그아웃 처리
        console.info('checkLoginStatus: User is not authenticated or session expired.');
        await logout(); // 스토어 상태 초기화 및 백엔드 로그아웃 호출
        return false; // 로그인 상태 false 반환
      } finally {
        isLoading.value = false;
      }
    }

    /**
     * 로그아웃 처리 액션.
     * 스토어 상태를 초기화하고, 백엔드 로그아웃 API 를 호출합니다.
     */
    async function logout() {
      const tagsViewStore = useTagsViewStore(); // <<< 스토어 인스턴스 가져오기

      try {
        await logoutApi(); // 백엔드 로그아웃 호출
      } catch (error) {
        console.error('Error during backend logout:', error);
      } finally {
        // --- !!! 성공/실패 관계없이 클라이언트 상태 초기화 !!! ---
        setUserInfo(null);

        // --- !!! TagsView 초기화 호출 !!! ---
        tagsViewStore.clearViews(); // <<< 이 부분 추가!

        // 로그인 페이지로 이동
        router.replace('/login').catch(err => {
          console.error('Error redirecting to login after logout:', err);
        });
      }
    }

    return {
      // State
      userInfo,
      isLoading,
      // Getters (Computed)
      isLoggedIn,
      username,
      empId,
      cmpCd,
      authorities,
      hasRole,
      hasAuthority,
      // Actions
      setUserInfo,
      checkLoginStatus,
      logout,
    };
  },
// --- (선택 사항) Pinia Persist 플러그인 설정 ---
{ persist: true } // 브라우저 저장소(localStorage)에 상태 자동 저장/복구
);
