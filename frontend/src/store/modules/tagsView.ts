import { defineStore } from 'pinia';
import type { TagView } from '@/types/tagsView';
import type {RouteLocationNormalized, RouteRecordRaw} from 'vue-router';
import router from '@/router';
import {computed, ref} from "vue";

export const useTagsViewStore = defineStore('tagsView', () => {
    // === State ===
    // ref() 를 사용하여 반응형 상태 정의
    const visitedViews = ref<TagView[]>([]);
    const cachedViews = ref<string[]>([]); // keep-alive 대상 컴포넌트 이름 목록 (라우트/컴포넌트의 name)

    // === Getters (필요한 경우 computed() 사용) ===
    // 예시: 고정 탭 목록 getter
    const affixTags = computed(() => visitedViews.value.filter(v => v.meta?.affix));

    // helper 함수 추가
    /**
     * 해당 뷰가 대시보드인지 확인
     * @param view TagView 객체
     * @returns 대시보드 여부
     */
    function isDashboard(view: TagView): boolean {
      // 경로가 '/dashboard' 이거나 라우트 이름이 'Dashboard' 인 경우
      return view.path === '/dashboard' || (typeof view.name === 'string' && view.name.toLowerCase() === 'dashboard');
    }

    /**
     * 해당 뷰가 고정 탭인지 확인 (meta.affix 사용)
     * @param view TagView 객체
     * @returns 고정 탭 여부
     */
    function isAffix(view: TagView): boolean {
      return view.meta?.affix === true;
    }

    /**
     * 해당 뷰가 닫기 가능한지 확인 (대시보드 아니고 고정 탭 아님)
     * @param view TagView 객체
     * @returns 닫기 가능 여부
     */
    function isTabClosable(view: TagView): boolean {
      return !isDashboard(view) && !isAffix(view);
    }

    // === Actions (일반 함수로 정의) ===
    function addView(view: RouteLocationNormalized) {

      addVisitedView(view);
      addCachedView(view);
    }

    function addVisitedView(view: RouteLocationNormalized) {

      // meta.hidden 속성이 있다면 추가하지 않음
      if (!view.name || !view.meta || view.meta.hidden) {
        return;
      }

      // title 속성이 없거나 빈 문자열이면 탭을 추가하지 않음
      if (!view.meta.title || String(view.meta.title).trim() === '') {
        return; // <<< title 없으면 여기서 함수 종료
      }
      // .value 를 통해 상태 접근
      if (visitedViews.value.some(v => v.path === view.path)) {
        // 이미 존재하면 업데이트만 시도할 수 있음 (updateVisitedView 호출)
        updateVisitedView(view); // <<< 파라미터/쿼리 변경 시 정보 업데이트
        return;
      }

      const title = String(view.meta.title);
      const newTagView: TagView = {
        fullPath: view.fullPath, // fullPath는 최신 정보로 저장해둠 (탭 클릭 시 이동용)
        path: view.path,         // path는 탭 식별 기준
        name: view.name,
        title: title,
        meta: { ...view.meta, title: title }
      };

      visitedViews.value.push(newTagView);
    }

    function addCachedView(view: RouteLocationNormalized) {
      if (!view.name || view.meta?.noCache) {
        return;
      }
      const componentName = view.name as string;
      // .value 를 통해 상태 접근
      if (cachedViews.value.includes(componentName)) return;
      cachedViews.value.push(componentName);
    }

    function delView(view: TagView) {
      return new Promise<{ visitedViews: TagView[], cachedViews: string[] }>(resolve => {
        if (!isTabClosable(view)) {
          resolve({ visitedViews: [...visitedViews.value], cachedViews: [...cachedViews.value] }); // 변경 없음 반환
          return;
        }
        delVisitedView(view);
        delCachedView(view);
        resolve({
          visitedViews: [...visitedViews.value],
          cachedViews: [...cachedViews.value]
        });
      });
    }

    function delVisitedView(view: TagView) {
      if (!isTabClosable(view)) {
        return;
      }
      const index = visitedViews.value.findIndex(v => v.path === view.path);
      if (index > -1) visitedViews.value.splice(index, 1);
    }

    function delCachedView(view: TagView) {
      if (!view.name) return;
      const componentName = view.name as string;
      const index = cachedViews.value.indexOf(componentName);
      if (index > -1) cachedViews.value.splice(index, 1);
    }

    function delOthersViews(view: TagView) {
      return new Promise<{ visitedViews: TagView[], cachedViews: string[] }>(resolve => {
        delOthersVisitedViews(view);
        delOthersCachedViews(view);

        resolve({
          visitedViews: [...visitedViews.value],
          cachedViews: [...cachedViews.value]
        });
      });
    }

    function delOthersVisitedViews(view: TagView) {
      // 고정(affix) 또는 현재(view) 탭만 남김
      visitedViews.value = visitedViews.value.filter(v => {
        return isAffix(v) || v.path === view.path;
      });
    }

    function delOthersCachedViews(view: TagView) {
      // 남은 visitedViews 기준으로 cachedViews 재설정
      cachedViews.value = visitedViews.value
        .filter(v => v.name && !v.meta?.noCache)
        .map(v => v.name as string);
    }

    function delAllViews() {
      // --- !!! Promise 반환 타입 수정 !!! ---
      return new Promise<{ visitedViews: TagView[], cachedViews: string[] }>(resolve => {
        delAllVisitedViews();
        delAllCachedViews();
        resolve({
          visitedViews: [...visitedViews.value],
          cachedViews: [...cachedViews.value]
        });
      });
    }

    function delAllVisitedViews() {
      // --- affixTags computed 값을 사용 ---
      const affixTagsValue = affixTags.value; // 고정 탭 목록 가져오기
      visitedViews.value = [...affixTagsValue]; // 고정 탭만 남도록 visitedViews 업데이트

    }

    function delAllCachedViews() {
      cachedViews.value = visitedViews.value
        .filter(v => v.name && !v.meta?.noCache)
        .map(v => v.name as string);
    }

    /**
     * 이미 방문한 뷰의 정보를 업데이트합니다 (예: 제목 변경, 파라미터 변경 등).
     * @param view 업데이트할 정보가 담긴 RouteLocationNormalized 객체
     */
    function updateVisitedView(view: RouteLocationNormalized) { // <<< 파라미터 타입 명확화
      const index = visitedViews.value.findIndex(v => v.path === view.path);

      if (index > -1) {
        const targetView = visitedViews.value[index]; // 업데이트 대상 탭
        const title = String(view.meta?.title || (typeof view.name === 'string' ? view.name : view.path)); // title 재계산

        const updatedView: TagView = {
          fullPath: view.fullPath, // 최신 fullPath 로 업데이트
          path: view.path,         // path 는 동일
          name: view.name,         // 최신 name 사용
          title: title,            // 계산된 title 사용
          meta: view.meta ? { ...view.meta, title: title } : targetView.meta // 최신 meta 사용 (title 포함)
        };

        visitedViews.value[index] = updatedView;

      } else {
        addView(view);
      }
    }

    function clearViews() {
      const affixTagsValue = affixTags.value;
      visitedViews.value = [...affixTagsValue]; // 고정 탭만 남김
      cachedViews.value = affixTagsValue
        .filter(v => v.name && !v.meta?.noCache)
        .map(v => v.name as string); // 고정 탭 캐시만 남김
    }

    function toLastView(closedView?: TagView) {
      /**
       * 탭이 닫힌 후, 이동해야 할 마지막 유효 뷰로 라우터를 이동시킵니다.
       * @param closedView 닫힌 탭 정보 (선택적)
       */
      // visitedViews 에서 마지막 탭 가져오기
      const latestView = visitedViews.value.slice(-1)[0];

      // 1. 마지막 탭이 존재하고, 그 탭이 방금 닫힌 탭이 아니라면 그 탭으로 이동
      if (latestView && latestView.fullPath && (!closedView || latestView.path !== closedView.path)) { // path 기준으로 비교
        router.push(latestView.fullPath); // 이동은 fullPath 로
      } else {
        // 2. 마지막 탭이 없거나, 그게 방금 닫힌 탭이라면:
        //    a. 첫 번째 고정 탭으로 이동 시도
        const firstAffixTag = affixTags.value[0];
        if (firstAffixTag && firstAffixTag.fullPath) {
          router.push(firstAffixTag.fullPath);
        } else {
          //    b. 고정 탭도 없다면 대시보드로 이동 (단, 현재 경로가 대시보드가 아닐 때만)
          if (router.currentRoute.value.path !== '/dashboard') {
            router.push('/dashboard');
          }
        }
      }
    }

    /**
     * 라우터 설정에서 고정(affix) 탭들을 찾아 초기화합니다.
     * title 이 있는 고정 탭만 추가합니다.
     * @param routes 전체 라우트 설정 배열 (readonly)
     */
    function initAffixTags(routes: readonly RouteRecordRaw[]) {
      const affixRouteTagsData = routes.filter(route => route.meta?.affix); // 고정 탭 필터링 (Optional Chaining 사용)

      for (const tag of affixRouteTagsData) {
        // --- !!! 변경 지점: title 존재 및 유효성 체크 추가 !!! ---
        if (tag.name && !tag.meta?.hidden && tag.meta?.title && String(tag.meta.title).trim() !== '') {
          // --------------------------------------------------
          // 라우트 정보를 RouteLocationNormalized 와 유사하게 만듦 (addView 가 받도록)
          const dummyRoute = {
            fullPath: tag.path, // 간단히 path 로 설정 (파라미터 없는 고정 탭 가정)
            path: tag.path,
            name: tag.name,
            meta: tag.meta
            // params, query 등은 필요시 추가 가능하나 보통 고정 탭은 단순 경로
          } as RouteLocationNormalized; // 타입 단언 사용
          addView(dummyRoute); // addView 호출 (내부에서 title 체크는 스킵될 수 있음)
        } else {
        }
      }
    }

    // === 반환: 스토어에서 사용할 상태, 게터, 액션을 객체로 반환 ===
    return {
      // State
      visitedViews,
      cachedViews,
      // Getters
      affixTags,
      // Actions
      addView,
      delView,
      delOthersViews,
      delAllViews,
      updateVisitedView,
      clearViews,
      toLastView,
      initAffixTags,
      delVisitedView,
      delCachedView,
      isDashboard,
      isAffix,
      isTabClosable
    };
  },
// { // 스토어 옵션 (필요시)
//   persist: true // 예시: 탭 상태 유지
// }
);
