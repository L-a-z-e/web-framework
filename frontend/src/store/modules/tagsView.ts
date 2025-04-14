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

    // === Actions (일반 함수로 정의) ===
    function addView(view: RouteLocationNormalized) {
      addVisitedView(view);
      addCachedView(view);
    }

    function addVisitedView(view: RouteLocationNormalized) {
      // meta.hidden 속성이 있다면 추가하지 않음
      if (view.meta?.hidden) {
        return;
      }
      // .value 를 통해 상태 접근
      if (visitedViews.value.some(v => v.fullPath === view.fullPath)) return;

      const title = view.meta.title ? String(view.meta.title) : 'No Title';
      const newView: TagView = {
        ...view,
        title: title};
      visitedViews.value.push(newView);
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
      return new Promise<void>(resolve => { // 반환 타입 명시 (Promise<void> 또는 다른 필요한 타입)
        delVisitedView(view);
        delCachedView(view);
        resolve(); // 액션 자체에서 반환할 값이 없다면 void Promise
      });
    }

    function delVisitedView(view: TagView) {
      const index = visitedViews.value.findIndex(v => v.fullPath === view.fullPath);
      if (index > -1) visitedViews.value.splice(index, 1);
    }

    function delCachedView(view: TagView) {
      if (!view.name) return;
      const componentName = view.name as string;
      const index = cachedViews.value.indexOf(componentName);
      if (index > -1) cachedViews.value.splice(index, 1);
    }

    function delOthersViews(view: TagView) {
      return new Promise<void>(resolve => {
        // .value 를 사용하여 상태 업데이트
        visitedViews.value = visitedViews.value.filter(v => v.meta?.affix || v.fullPath === view.fullPath);
        cachedViews.value = visitedViews.value
          .filter(v => v.name && !v.meta?.noCache)
          .map(v => v.name as string);
        resolve();
      });
    }

    function delAllViews() {
      return new Promise<void>(resolve => {
        // .value 사용
        const affixTagsValue = affixTags.value; // computed 값 사용
        visitedViews.value = affixTagsValue;
        cachedViews.value = affixTagsValue
          .filter(v => v.name && !v.meta?.noCache)
          .map(v => v.name as string);
        resolve();
      });
    }

    function updateVisitedView(view: TagView) {
      const index = visitedViews.value.findIndex(v => v.fullPath === view.fullPath);
      if (index > -1) {
        // 기존 뷰 객체와 새 뷰 객체를 병합하여 업데이트
        visitedViews.value[index] = { ...visitedViews.value[index], ...view };
      }
    }

    function clearViews() {
      visitedViews.value = [];
      cachedViews.value = [];
    }

    function toLastView(currentView?: TagView) {
      // .value 사용
      const latestView = visitedViews.value.slice(-1)[0];
      if (latestView && latestView.fullPath) {
        router.push(latestView.fullPath);
      } else {
        const firstAffixTag = affixTags.value[0]; // computed 값 사용
        if (firstAffixTag && firstAffixTag.fullPath) {
          router.push(firstAffixTag.fullPath);
        } else {
          router.push(currentView?.name === 'Dashboard' ? '/' : '/dashboard');
        }
      }
    }

    // (선택적) 고정 탭 초기 추가 로직
    function initAffixTags(routes: readonly RouteRecordRaw[]) {
      const affixRouteTags = routes.filter(route => route.meta?.affix);
      for (const tag of affixRouteTags) {
        if (tag.name && !tag.meta?.hidden) { // 이름 있고 숨김 아닌 탭만
          // 라우트 정보를 기반으로 TagView 객체 생성 (타입 캐스팅 주의)
          const dummyRoute = {
            fullPath: tag.path,
            path: tag.path,
            name: tag.name,
            meta: tag.meta
          } as RouteLocationNormalized; // 필요한 속성만 포함하여 타입 맞춤
          addView(dummyRoute); // addView 호출하여 visited/cached 동시 처리
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
      // 개별 삭제 함수도 외부에서 필요하면 노출
      delVisitedView,
      delCachedView,
    };
  },
// { // 스토어 옵션 (필요시)
//   persist: true // 예시: 탭 상태 유지
// }
);
