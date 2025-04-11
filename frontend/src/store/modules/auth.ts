// src/store/auth.ts
import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import type {UserInfo} from "@/types/user.ts";

// --- !!! Backend 의 UserDetails 구현체 타입 임포트 !!! ---
// 실제 경로와 타입 이름을 확인하고 맞게 수정해야 합니다.
// 예시: import type { CustomUserDetails } from '@/types/user';
// 만약 타입을 공유하기 어렵다면, 필요한 필드만 포함하는 인터페이스를 frontend 에서 정의해도 됩니다.

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
    const empId = computed(() => userInfo.value?.empId || null);
    // 회사 코드
    const cmpCd = computed(() => userInfo.value?.cmpCd || null);
    // 권한 목록 (GrantedAuthority 객체에서 실제 권한 문자열만 추출)
    const authorities = computed(() => userInfo.value?.authorities?.map(auth => auth.authority) || []);

    // 특정 권한을 가지고 있는지 확인하는 getter 예시
    function hasRole(role: string): boolean {
      // 'ROLE_' 접두사 자동 추가 또는 확인 로직 포함 가능
      const roleName = role.startsWith('ROLE_') ? role : `ROLE_${role}`;
      return authorities.value.includes(roleName);
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
     * (세션 기반에서는 이 액션의 역할이 다름)
     * 애플리케이션 시작 시 또는 페이지 새로고침 시 서버에 현재 로그인 상태(세션 유효성) 및
     * 사용자 정보를 요청하여 스토어를 초기화하는 액션.
     * @returns Promise<boolean> 로그인 상태 여부
     */
    async function checkLoginStatus() {
      if (isLoggedIn.value) return true; // 이미 유저 정보가 있으면 체크 불필요 (선택적)

      isLoading.value = true;
      try {
        // TODO: 백엔드에 현재 사용자 정보를 가져오는 API 호출 ('/api/user/me' 등)
        // 예시: const response = await api.get('/api/user/me');
        // if (response.data.success && response.data.data) {
        //   setUserInfo(response.data.data); // API 응답 데이터로 사용자 정보 설정
        //   return true;
        // } else {
        //   // API 호출 실패 또는 사용자 정보 없음 (세션 만료 등)
        //   await logout(); // 로그아웃 처리
        //   return false;
        // }
        console.warn('checkLoginStatus(): API call not implemented yet.');
        // 임시: 테스트를 위해 강제로 사용자 정보 설정 (실제 구현 시 제거)
        if (localStorage.getItem('tempLoggedIn') === 'true') {
          const tempUser: UserInfo = { empId: 'testuser', empNm: '테스트', cmpCd: 'C001', deptCd: 'D001', authorities: [{ authority: 'ROLE_USER' }] };
          setUserInfo(tempUser);
          return true;
        } else {
          return false;
        }
      } catch (error) {
        console.error('Error checking login status:', error);
        await logout(); // 에러 발생 시 로그아웃 처리
        return false;
      } finally {
        isLoading.value = false;
      }
    }

    /**
     * 로그아웃 처리 액션.
     * 스토어 상태를 초기화하고, 백엔드 로그아웃 API 를 호출합니다.
     */
    async function logout() {
      console.log('Performing logout action...');
      setUserInfo(null); // 스토어의 사용자 정보 제거
      localStorage.removeItem('tempLoggedIn'); // 임시 로그인 상태 제거

      try {
        // TODO: 백엔드 로그아웃 API 호출 ('/logout' POST)
        // 예시: await api.post('/logout');
        console.warn('logout(): Backend API call not implemented yet.');
      } catch (error) {
        console.error('Error during backend logout:', error);
        // 에러가 발생해도 프론트엔드 상태는 초기화된 상태 유지
      }
    }

    // 스토어에서 외부로 노출할 상태, getter, action 반환
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
// { persist: true } // 브라우저 저장소(localStorage)에 상태 자동 저장/복구
);
