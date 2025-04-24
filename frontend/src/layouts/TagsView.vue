<template>
  <div id="tags-view-container" class="tags-view-container">
    <!-- Element Plus Tabs 컴포넌트 사용 -->
    <el-tabs
      v-model="activeTab"
      type="card"
      class="tags-view-tabs"
      :closable="true"
      @tab-click="handleTabClick"
      @tab-remove="handleTabRemove"
    >
      <!-- 스토어의 visitedViews 를 기반으로 탭 동적 생성 -->
      <!-- 고정 탭은 닫기 버튼 비활성화 -->
      <el-tab-pane
        v-for="tag in visitedViews"
        :key="tag.path"
        :label="tag.title"
        :name="tag.fullPath || tag.path"
        :closable="!isTabClosable(tag)"
      >
      <!-- 우클릭 컨텍스트 메뉴를 위해 라벨 슬롯 사용 -->
      <template #label>
          <span @contextmenu.prevent="openMenu(tag, $event)">
            {{ tag.title }}
          </span>
      </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 우클릭 컨텍스트 메뉴 -->
    <ul v-show="visible" :style="{ left: left + 'px', top: top + 'px' }" class="contextmenu">
      <li @click="refreshSelectedTag(selectedTag)">Refresh</li>
      <li v-if="isTabClosable(selectedTag)" @click="closeSelectedTag(selectedTag)">Close</li>
      <li @click="closeOthersTags">Close Others</li>
      <li @click="closeAllTags(selectedTag)">Close All</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useTagsViewStore } from '@/store/modules/tagsView'; // TagsView 스토어
import { useAuthStore } from "@/store";
import type { TagView } from '@/types/tagsView';
import { ElTabs, ElTabPane } from 'element-plus'; // ElTabs, ElTabPane 임포트
import type { TabsPaneContext } from 'element-plus';
import {storeToRefs} from "pinia"; // 타입 임포트

// 스토어, 라우터, 현재 라우트 인스턴스
const store = useTagsViewStore();
const authStore = useAuthStore();
const router = useRouter();
const route = useRoute();

const { visitedViews, affixTags } = storeToRefs(store);
const {
  isDashboard, isAffix, isTabClosable, // 스토어 헬퍼 함수 사용
  addView, updateVisitedView, // 추가/업데이트 함수
  delView, delOthersViews, delAllViews, // 삭제 함수
  delCachedView, // 새로고침 시 캐시 삭제용
  clearViews, // <<< 로그아웃 시 사용
  toLastView, initAffixTags // 이동 및 초기화
} = store;

const activeTab = ref(route.fullPath);

// --- 컨텍스트 메뉴 관련 상태 및 함수 (이전 구현과 대부분 동일) ---
const visible = ref(false);
const top = ref(0);
const left = ref(0);
const selectedTag = ref<TagView>({});

/**
 * ElTabs의 @tab-click 이벤트 핸들러. 탭 클릭 시 해당 경로로 라우터 이동.
 * @param pane 클릭된 탭의 컨텍스트 정보 (TabsPaneContext)
 */
const handleTabClick = (pane: TabsPaneContext) => {
  const clickedFullPath = pane.props.name;
  if (typeof clickedFullPath === 'string' && clickedFullPath !== route.fullPath) {
    router.push(clickedFullPath); // 현재 경로와 다른 경우에만 이동
  }
};

/**
 * ElTabs @tab-remove 핸들러 (또는 컨텍스트 메뉴의 Close)
 */
const handleTabRemove = async (targetName: string | number) => {
  if (typeof targetName !== 'string') return;
  const targetView = visitedViews.value.find(view => view.fullPath === targetName);
  if (targetView) {
    const isCurrentTab = activeTab.value === targetName;

    // 삭제 전에 이동할 경로 미리 찾기
    let nextPath = '/dashboard';
    if (isCurrentTab && visitedViews.value.length > 1) {
      // 현재 탭의 인덱스 찾기
      const index = visitedViews.value.findIndex(v => v.fullPath === targetName);
      // 이전 탭이나 다음 탭 선택 (이전 탭 우선)
      const nextView = index > 0
        ? visitedViews.value[index - 1]
        : visitedViews.value.find((_, i) => i !== index);

      if (nextView) {
        nextPath = (nextView.fullPath as string) || (nextView.path as string);
      }
    }

    // 탭 삭제
    await delView(targetView);

    // 현재 활성 탭이 삭제된 경우 명시적으로 라우터 이동 실행
    if (isCurrentTab) {
      await router.push(nextPath);
      activeTab.value = nextPath; // 활성 탭도 명시적 업데이트
    }
  }
};


// 컨텍스트 메뉴 열기
const openMenu = (tag: TagView, e: MouseEvent) => {
  const menuMinWidth = 105;
  const container = document.getElementById('tags-view-container');
  if (!container) return;
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
  const containerRect = container.getBoundingClientRect();
  let calculatedLeft = rect.left - containerRect.left + 15;
  const maxLeft = container.clientWidth - menuMinWidth;
  if (calculatedLeft > maxLeft) calculatedLeft = maxLeft;
  let calculatedTop = rect.bottom - containerRect.top;
  left.value = calculatedLeft;
  top.value = calculatedTop;
  visible.value = true;
  selectedTag.value = tag;
};

// 컨텍스트 메뉴 닫기
const closeMenu = () => { visible.value = false; };

// 선택된 탭 새로고침 (keep-alive 캐시 제거 후 동일 경로 replace)
const refreshSelectedTag = (view: TagView) => {
  if (!view.fullPath) return;
  // !!! 스토어의 delCachedView 호출 !!!
  delCachedView(view);
  nextTick(() => {
    router.replace({ path: '/redirect' + view.fullPath }).catch(() => {});
  });
  closeMenu();
};

// 컨텍스트 메뉴: Close (handleTabRemove 재사용)
const closeSelectedTag = (view: TagView) => {
  handleTabRemove(view.fullPath || '');
  closeMenu();
};

// 컨텍스트 메뉴: Close Others
const closeOthersTags = () => {
  if (!selectedTag.value.fullPath) return;
  router.push(selectedTag.value.fullPath);
  delOthersViews(selectedTag.value);
  closeMenu();
};

// 컨텍스트 메뉴: Close All
const closeAllTags = (view: TagView) => {
  delAllViews().then(() => {
    toLastView(view);
  });
  closeMenu();
};

// --- Watchers ---

// 라우트 변경 감지
watch(route, (toRoute) => {
  if (toRoute.name) {
    // addView 내부에서 중복 체크 및 업데이트 호출하도록 수정
    addView(toRoute);
  }
  // 현재 활성 탭 업데이트
  activeTab.value = toRoute.fullPath;
}, { immediate: true });

// 컨텍스트 메뉴 외부 클릭 시 닫기
watch(visible, (value) => {
  if (value) {
    document.body.addEventListener('click', closeMenu);
  } else {
    document.body.removeEventListener('click', closeMenu);
  }
});

// 스토어 탭 목록 변경 감지 (안정성)
watch(visitedViews, (newViews) => {
  // 현재 활성 탭(activeTab)이 visitedViews 에 없으면 마지막 뷰로 이동
  if (!newViews.some(view => view.fullPath === activeTab.value)) {
    toLastView(); // 스토어의 toLastView 호출
  } else if (newViews.length === 0 && route.path !== '/dashboard') {
    // 탭이 모두 닫혔고 대시보드가 아니면 대시보드로 (스토어 로직과 일치 확인)
    if (router.currentRoute.value.path !== '/dashboard') {
      router.push('/dashboard');
    }
  }
}, { deep: true });

// !!! 인증 상태 변화 감지 (로그아웃 시 탭 초기화) !!!
watch(() => authStore.isLoggedIn, (isLoggedIn, wasLoggedIn) => {
  console.log('Auth state changed:', wasLoggedIn, '->', isLoggedIn); // 로그 추가
  if (wasLoggedIn && !isLoggedIn) {
    clearViews(); // <<< 스토어의 clearViews 호출
  }
});
// ------------------------------------------

// --- Lifecycle Hooks ---
onMounted(() => {
  // 컴포넌트 마운트 시 고정 탭 초기화
  initAffixTags(router.getRoutes()); // <<< 스토어의 initAffixTags 호출
  // 현재 라우트 activeTab 설정
  activeTab.value = route.fullPath;
});

</script>

<style scoped>
/* TagsView 컨테이너 기본 스타일 */
.tags-view-container {
  box-sizing: border-box !important;
  height: var(--tagview-height);
  background-color: var(--tagview-bg-color);
  padding: 0 10px;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, .12), 0 0 3px 0 rgba(0, 0, 0, .04);
  position: relative;
  border-bottom: 1px solid var(--app-border-color);
}

/* ElTabs 컴포넌트 스타일 조정 */
.tags-view-tabs {
  height: 100%; /* 컨테이너 높이 채우기 */
}

/* Tabs 헤더 영역 */
:deep(.el-tabs__header) {
  margin: 0; /* 기본 마진 제거 */
  border-bottom: none; /* 기본 하단 테두리 제거 */
  height: 100%; /* 높이 100% */
  display: flex;
}

/* 네비게이션 스크롤 버튼 (탭 많을 때) */
:deep(.el-tabs__nav-prev),
:deep(.el-tabs__nav-next) {
  line-height: var(--tagview-height);
  color: var(--app-text-light-color);
}
:deep(.el-tabs__nav-prev:hover),
:deep(.el-tabs__nav-next:hover) {
  color: var(--button-primary-color);
}

/* 개별 탭 아이템 */
:deep(.el-tabs__item) {
  box-sizing: border-box;
  height: calc(var(--tagview-height) - 8px);
  line-height: calc(var(--tagview-height) - 8px);
  font-size: 13px;
  margin-top: 4px;
  margin-bottom: 4px;
  margin-right: 5px;
  border: 1px solid var(--app-border-color) !important;
  border-radius: 4px;
  padding: 0 15px !important;
  color: var(--tagview-text-color);
  background-color: var(--form-bg-color);
  transition: color 0.3s, background-color 0.3s, border-color 0.3s;
}
/* 탭 호버 스타일 */
:deep(.el-tabs__item:hover) {
  color: var(--button-primary-hover);
  border-color: var(--button-primary-hover) !important;
}

/* 활성 탭 스타일 */
:deep(.el-tabs__item.is-active) {
  color: var(--tagview-active-text);
  border-color: var(--tagview-active-bg) !important;
  background-color: var(--tagview-active-bg);
  box-shadow: 0 0 3px rgba(0, 0, 0, 0.1);
}

/* 탭 닫기 버튼 */
:deep(.el-tabs__item .el-icon) { /* ElIcon 컴포넌트 타겟 */
  margin-left: 8px;
  vertical-align: middle;
  border-radius: 50%;
  padding: 1px;
  transition: all 0.3s cubic-bezier(.645,.045,.355,1);
  color: var(--icon-color);
}
/* 닫기 버튼 호버 시 */
:deep(.el-tabs__item .el-icon:hover) {
  background-color: var(--button-danger-color);
  color: var(--button-text-color);
}

:deep(.el-tabs__item.is-active .el-icon:hover) {
  background-color: rgba(255, 255, 255, 0.3); /* 약간 다른 호버 효과 */
  color: var(--tagview-active-text);
}

/* 컨텍스트 메뉴 스타일 (이전과 동일) */
.contextmenu {
  margin: 0;
  background: var(--card-bg-color);
  color: var(--app-text-color);
  box-shadow: 2px 2px 3px 0 rgba(0, 0, 0, .3); /* 다크모드 그림자 유지 또는 조정 */
  z-index: 3000;
  position: absolute;
  list-style-type: none;
  padding: 5px 0;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 400;
}
.contextmenu li {
  margin: 0;
  padding: 7px 16px;
  cursor: pointer;
}
.contextmenu li:hover {
  background: var(--sidebar-hover-bg);
}
</style>
