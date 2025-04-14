import { createRouter, createWebHistory } from 'vue-router';
import type { RouteRecordRaw } from 'vue-router';
import {useAuthStore, useMenuStore, useTagsViewStore} from "@/store";
import type {MenuInfo} from "@/types/menu.ts";

const MainLayout = () => import('@/layouts/MainLayout.vue');
const Login = () => import('@/views/auth/Login.vue');
const Dashboard = () => import('@/views/dashboard/Dashboard.vue');
// const Menu = () => import('@/views/menu/Menu.vue'); // 동적 import로 변경
// const Sample = () => import('@/views/sample/Sample.vue');
const viewModules = import.meta.glob('@/views/**/*.vue');

// --- 라우트 정의 (경로도 필요시 수정) ---
const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    name: 'MainLayout',
    component: MainLayout,
    redirect: '/dashboard', // 기본 접속 시 대시보드로 이동
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard', // '/dashboard' 로 접근
        name: 'Dashboard',
        component: Dashboard,
        meta: {
          title: 'Dashboard',
          icon: 'House',
          requiresAuth: true,
          affix: true,
          noCache: false,
        }

      },
    ]
  },
  // 404 Not Found 페이지 (가장 마지막에 정의)
  // {
  //   path: '/:pathMatch(.*)*', // 정의되지 않은 모든 경로
  //   name: 'NotFound',
  //   component: NotFound,
  //   meta: { title: '페이지를 찾을 수 없음' }
  // },
];

/**
 * MenuInfo 객체를 받아 Vue Router 라우트 정의 객체(RouteRecordRaw)를 생성합니다.
 * MENU_ID 규칙: 앞 두자리는 디렉토리 경로, 전체는 파일 이름 (예: FW1101 -> /src/views/fw/FW1101.vue)
 * @param menu 메뉴 정보 객체
 * @returns 생성된 RouteRecordRaw 객체 또는 컴포넌트를 찾지 못하면 null
 */
function createRoute(menu: MenuInfo): RouteRecordRaw | null {
  const menuId = menu.menuId; // 예: "FW1101", "CO2101"

  // 메뉴 ID 가 유효한지 기본적인 확인 (예: 최소 길이 등)
  if (!menuId || menuId.length < 3 || menuId.startsWith('#')) { // '#' 으로 시작하는 그룹 ID 제외
    // console.warn(`Invalid or group menuId, skipping route creation: ${menuId}`);
    return null; // 그룹 메뉴 등은 라우트 생성 안 함
  }

  // 메뉴 ID 앞 두 글자를 추출하여 소문자로 변환 -> 디렉토리 경로로 사용
  const directory = menuId.substring(0, 2).toLowerCase();

  // Vite Glob Import 에서 사용할 컴포넌트 파일 경로 생성
  // 키 형식은 '/src/views/...' 이어야 함
  const componentPath = `/src/views/${directory}/${menuId}.vue`; // 예: /src/views/fw/FW1101.vue

  console.log(`Attempting to find component for menuId '${menuId}' at path: ${componentPath}`);

  // viewModules 에 해당 경로의 컴포넌트 로더 함수가 있는지 확인
  if (viewModules[componentPath]) {
    console.log(`Component found for ${menuId}. Creating route...`);
    return {
      // 라우트 경로는 menuId 앞에 '/' 를 붙여 사용 (예: /FW1101)
      path: '/' + menuId,
      name: menuId, // 라우트 이름은 menuId 그대로 사용
      component: viewModules[componentPath], // 찾은 컴포넌트 로더 함수 지정
      meta: {
        title: menu.menuNm,
        icon: menu.menuIcon,
        requiresAuth: true,
        affix: false,
        noCache: false,
      }
    };
  } else {
    // 해당 경로에 컴포넌트 파일이 존재하지 않음
    console.warn(`Component not found for menuId: ${menuId} at resolved path: ${componentPath}. Skipping route creation.`);
    return null; // 라우트 생성 안 함
  }
}

let hasAddedRoutes = false; // 라우트 중복 추가 방지 플래그

function addDynamicRoutes(menuItems: MenuInfo[]) {
  if (hasAddedRoutes) {
    console.log('Dynamic routes have already been added.');
    return;
  }

  console.log('Adding dynamic routes...');

  function addRoutesRecursive(menuItem: MenuInfo) {
    // 1. 현재 메뉴 항목에 대한 라우트 생성 시도 (페이지 없는 그룹 메뉴('#') 등은 createRoute 에서 null 반환)
    const hasPage = menuItem.menuId && !menuItem.menuId.startsWith('#') && (!menuItem.children || menuItem.children.length === 0);
    // 2. 유효한 라우트 객체가 생성되었으면 라우터에 추가
    if (hasPage) {
      const route = createRoute(menuItem);
      if (route) {
        try {
          router.addRoute('MainLayout', route);
          console.log(`Successfully added route: ${route.path} (name: ${String(route.name)})`);
        } catch (error) {
          // 라우트 추가 중 에러 발생 시 (예: 이름 중복) 로그 기록
          console.error(`Failed to add route for ${String(route.name) || route.path}:`, error);
        }
      }
    }

    // 3. 하위 메뉴가 있으면 재귀적으로 처리
    if (menuItem.children && menuItem.children.length > 0) {
      menuItem.children.forEach(child => addRoutesRecursive(child));
    }
  }

  // 최상위 메뉴 목록부터 재귀 함수 시작
  menuItems.forEach(menu => addRoutesRecursive(menu));

  // 라우트 추가 완료 플래그 설정
  hasAddedRoutes = true;
  console.log('Finished adding dynamic routes.');
}

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition;
    } else {
      return { top: 0 };
    }
  },
});

// --- Navigation Guard setting ---
let isInitialAuthCheckDone = false; // 앱 로딩 시 checkLoginStatus 중복 호출 방지 플래그
let hasFetchedMenus = false;

router.beforeEach(async (to, from, next) => {
  console.log(`Navigating from ${from.fullPath} to ${to.fullPath}`);

  const authStore = useAuthStore();
  const menuStore = useMenuStore();

  // --- 1. 앱 초기 로딩 시 또는 새로고침 시 로그인 상태 확인 ---
  // 아직 확인 전이고 & 로컬 스토리지 등에 상태가 남아있을 가능성이 있다면 서버에 확인
  if (!isInitialAuthCheckDone && !authStore.isLoggedIn && localStorage.getItem('auth') && !authStore.isLoading) {
    console.log('Initial auth check needed. Calling checkLoginStatus...');
    try {
      await authStore.checkLoginStatus(); // 서버에 세션 유효성 및 사용자 정보 요청
      console.log('Initial auth check completed. Logged in:', authStore.isLoggedIn);
    } catch (error) {
      console.error('Error during initial auth check:', error);
    } finally {
      isInitialAuthCheckDone = true;
    }
  } else if (!isInitialAuthCheckDone) {
    isInitialAuthCheckDone = true;
  }

  // --- 2. 라우트 접근 제어 ---
  const isLoggedIn = authStore.isLoggedIn;   // 현재 로그인 상태 확인

  if (isLoggedIn && !hasAddedRoutes) {
    console.log('User logged in, attempting to add dynamic routes...');
    try {
      if (menuStore.menuItems.length === 0 && !menuStore.isLoading) {
        await menuStore.fetchMenus();
      }
      addDynamicRoutes(menuStore.menuItems);

      // --- 라우트 추가 후 목표 경로로 다시 네비게이션 시도 ---
      console.log(`Routes added (attempted). Retrying navigation to: ${to.fullPath}`);
      next({ ...to, replace: true });
      return; // 현재 가드 종료

    } catch (error) {
      console.error('Error during menu fetch or route add:', error);
      // 에러 발생 시 처리 (예: 로그인 페이지로)
      hasAddedRoutes = false; // 실패했으므로 다시 시도 가능하게
      next({ name: 'Login' });
      return;
    }
  }

  const requiresAuth = to.meta.requiresAuth !== false; // 인증 필요 여부 확인

  if (requiresAuth && !isLoggedIn) {
    // 인증 필요한데 로그인 안됨 -> 로그인 페이지로
    console.log('Redirecting to login page (auth required).');
    menuStore.clearMenus();
    hasAddedRoutes = false;
    next({ name: 'Login', query: { redirect: to.fullPath } });
  } else if (to.name === 'Login' && isLoggedIn) {
    // 로그인했는데 로그인 페이지 -> 대시보드
    console.log('Already logged in. Redirecting to dashboard.');
    next({ path: '/' });
  } else {
    // 그 외 (정상 진행 또는 동적 라우트 추가 후 매칭 실패 시 - 404 처리 필요)
    // router.hasRoute(to.name || '') 로 존재 여부 확인 가능
    if (to.matched.length === 0 && to.path !== '/') { // '/' 는 리다이렉션 되므로 제외
      console.warn(`Route not found after dynamic add: ${to.fullPath}. Redirecting to NotFound or Home.`);
      // next({ name: 'NotFound' }); // 404 페이지 구현 시
      next({ path: '/' }); // 임시로 홈으로 이동
    } else {
      console.log('Proceeding with navigation.');
      next(); // 정상 진행
    }
  }
});

router.afterEach((to) => {
  // 네비게이션 성공 후 실행
  if (to.name && !to.meta?.hidden) { // 이름이 있고 숨김 처리되지 않은 라우트만 처리
    // 스토어 인스턴스 가져오기 (afterEach는 setup 컨텍스트 외부)
    const tagsViewStore = useTagsViewStore();
    tagsViewStore.addView(to); // 현재 방문한 뷰를 스토어에 추가
  }
});

export default router;
