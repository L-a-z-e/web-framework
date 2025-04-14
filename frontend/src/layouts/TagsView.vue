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
        :key="tag.fullPath"
        :label="tag.title"
        :name="tag.fullPath"
        :closable="!isAffix(tag)"
      >
      <!-- 우클릭 컨텍스트 메뉴를 위해 라벨 슬롯 사용 -->
      <template #label>
          <span @contextmenu.prevent="openMenu(tag, $event)">
            {{ tag.title }}
          </span>
      </template>
      </el-tab-pane>
    </el-tabs>

    <!-- 우클릭 컨텍스트 메뉴 (UI는 이전과 동일) -->
    <ul v-show="visible" :style="{ left: left + 'px', top: top + 'px' }" class="contextmenu">
      <li @click="refreshSelectedTag(selectedTag)">Refresh</li>
      <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)">Close</li>
      <li @click="closeOthersTags">Close Others</li>
      <li @click="closeAllTags(selectedTag)">Close All</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useTagsViewStore } from '@/store/modules/tagsView'; // TagsView 스토어
import type { TagView } from '@/types/tagsView';
import { ElTabs, ElTabPane } from 'element-plus'; // ★ ElTabs, ElTabPane 임포트
import type { TabsPaneContext } from 'element-plus'; // 타입 임포트

// 스토어, 라우터, 현재 라우트 인스턴스
const store = useTagsViewStore();
const router = useRouter();
const route = useRoute();

// 스토어에서 방문한 탭 목록 가져오기
const visitedViews = computed(() => store.visitedViews);

// ElTabs와 연결될 현재 활성 탭의 식별자 (fullPath 사용)
const activeTab = ref(route.fullPath);

// --- 컨텍스트 메뉴 관련 상태 및 함수 (이전 구현과 대부분 동일) ---
const visible = ref(false);
const top = ref(0);
const left = ref(0);
const selectedTag = ref<TagView>({});

// 탭이 고정 탭인지 확인
const isAffix = (tag: TagView): boolean => tag.meta?.affix === true;

/**
 * ElTabs의 @tab-click 이벤트 핸들러. 탭 클릭 시 해당 경로로 라우터 이동.
 * @param pane 클릭된 탭의 컨텍스트 정보 (TabsPaneContext)
 */
const handleTabClick = (pane: TabsPaneContext) => {
  // pane.props.name 에 우리가 ElTabPane의 :name으로 설정한 fullPath가 들어옴
  const clickedFullPath = pane.props.name;
  if (typeof clickedFullPath === 'string' && clickedFullPath !== route.fullPath) {
    router.push(clickedFullPath); // 현재 경로와 다른 경우에만 이동
  }
};

/**
 * ElTabs의 @tab-remove 이벤트 핸들러. 탭 닫기 버튼 클릭 시 호출.
 * @param targetName 닫으려는 탭의 name (fullPath)
 */
const handleTabRemove = (targetName: string | number) => {
  if (typeof targetName !== 'string') return;

  const targetView = visitedViews.value.find(view => view.fullPath === targetName);
  if (targetView) {
    store.delView(targetView).then(() => {
      // 닫힌 탭이 현재 활성 탭이었다면, 마지막 탭 또는 기본 경로로 이동
      if (activeTab.value === targetName) {
        store.toLastView(targetView);
      }
    });
  }
};

// 컨텍스트 메뉴 열기 (위치 계산 로직은 이전과 동일)
const openMenu = (tag: TagView, e: MouseEvent) => {
  const menuMinWidth = 105;
  const container = document.getElementById('tags-view-container');
  if (!container) return;
  // 클릭된 span 요소의 위치를 기준으로 계산
  const rect = (e.currentTarget as HTMLElement).getBoundingClientRect();
  const containerRect = container.getBoundingClientRect();

  let calculatedLeft = rect.left - containerRect.left + 5; // 라벨 시작점 기준
  const maxLeft = container.clientWidth - menuMinWidth;
  if (calculatedLeft > maxLeft) calculatedLeft = maxLeft;

  let calculatedTop = rect.bottom - containerRect.top + 5; // 라벨 하단 기준

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
  store.delCachedView(view); // 스토어에서 캐시 제거 액션 호출
  nextTick(() => {
    // 현재 활성 탭이면 replace로, 아니면 push 후 다시 원래 탭으로?
    // 일단 replace로 시도 (같은 경로 이동은 에러 발생 가능하나 무시)
    router.replace({ path: view.fullPath }).catch(() => {});
  });
};

// 컨텍스트 메뉴: Close (handleTabRemove 재사용)
const closeSelectedTag = (view: TagView) => {
  handleTabRemove(view.fullPath || '');
};

// 컨텍스트 메뉴: Close Others
const closeOthersTags = () => {
  if (!selectedTag.value.fullPath) return;
  router.push(selectedTag.value); // 선택된 탭으로 먼저 이동
  store.delOthersViews(selectedTag.value); // 스토어 액션 직접 호출
};

// 컨텍스트 메뉴: Close All
const closeAllTags = (view: TagView) => {
  store.delAllViews().then(() => {
    store.toLastView(view); // 남은 탭(고정 탭) 또는 기본 경로로 이동
  });
};

// --- Watchers ---

// 라우트 변경 감지 -> ElTabs의 활성 탭 업데이트
watch(() => route.fullPath, (newFullPath) => {
  activeTab.value = newFullPath;
  // ElTabs는 일반적으로 활성 탭을 자동으로 보이게 스크롤하지만,
  // 필요하다면 여기서 ElTabs 인스턴스에 접근하여 수동 스크롤 로직 추가 가능
});

// 컨텍스트 메뉴 외부 클릭 시 닫기 (이전과 동일)
watch(visible, (value) => {
  if (value) {
    document.body.addEventListener('click', closeMenu);
  } else {
    document.body.removeEventListener('click', closeMenu);
  }
});

// 스토어의 탭 목록 변경 시, 현재 활성 탭이 유효하지 않으면 이동 (안정성)
watch(visitedViews, (newViews) => {
  if (newViews.length > 0 && !newViews.some(view => view.fullPath === activeTab.value)) {
    store.toLastView(); // 마지막 유효 탭으로 이동
  } else if (newViews.length === 0 && route.path !== '/dashboard') { // 탭이 모두 닫혔고 대시보드가 아니면
    router.push('/dashboard'); // 예시: 대시보드로 이동
  }
}, { deep: true });


// --- Lifecycle Hooks ---
onMounted(() => {
  // 컴포넌트 마운트 시 현재 라우트 경로로 activeTab 초기화
  activeTab.value = route.fullPath;
});

</script>

<style scoped>
/* TagsView 컨테이너 기본 스타일 */
.tags-view-container {
  height: 40px; /* ElTabs 높이에 맞춰 조정 (기본값 근사치) */
  background-color: #fff;
  padding: 0 10px; /* 좌우 여백 */
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, .12), 0 0 3px 0 rgba(0, 0, 0, .04);
  position: relative; /* 컨텍스트 메뉴 포지셔닝 기준 */
  /* border-bottom: 1px solid var(--el-border-color-light); */ /* 필요시 하단 구분선 */
}

/* ElTabs 컴포넌트 스타일 조정 */
.tags-view-tabs {
  height: 100%; /* 컨테이너 높이 채우기 */
}

/* Tabs 헤더 영역 (탭들이 있는 곳) */
:deep(.el-tabs__header) {
  margin: 0; /* 기본 마진 제거 */
  border-bottom: none; /* 기본 하단 테두리 제거 */
  height: 100%; /* 높이 100% */
  display: flex;
  align-items: center; /* 탭들 세로 중앙 정렬 */
}

/* 네비게이션 스크롤 버튼 (탭 많을 때) */
:deep(.el-tabs__nav-prev),
:deep(.el-tabs__nav-next) {
  line-height: 40px; /* 컨테이너 높이에 맞춤 */
  color: #5a5e66;
}
:deep(.el-tabs__nav-prev:hover),
:deep(.el-tabs__nav-next:hover) {
  color: var(--el-color-primary);
}


/* 개별 탭 아이템 */
:deep(.el-tabs__item) {
  height: 30px; /* 탭 높이 조정 (컨테이너 높이보다 작게) */
  line-height: 30px; /* 라인 높이 맞춤 */
  font-size: 13px; /* 폰트 크기 */
  margin-right: 5px; /* 탭 간 간격 */
  border: 1px solid var(--el-border-color-light) !important; /* 테두리 명시 */
  border-radius: 4px; /* 모서리 둥글게 */
  padding: 0 15px !important; /* 내부 좌우 패딩 조정 */
  color: #5a5e66; /* 기본 텍스트 색상 */
  background-color: #ffffff; /* 기본 배경색 */
  transition: color 0.3s, background-color 0.3s; /* 부드러운 전환 효과 */
}
/* 탭 호버 스타일 */
:deep(.el-tabs__item:hover) {
  color: var(--el-color-primary);
  border-color: var(--el-color-primary-light-7);
}

/* 활성 탭 스타일 */
:deep(.el-tabs__item.is-active) {
  color: var(--el-color-primary); /* 활성 탭 텍스트 색상 */
  border-color: var(--el-color-primary) !important; /* 활성 탭 테두리 색상 */
  background-color: var(--el-color-primary-light-9); /* 활성 탭 배경색 (연하게) */
  /* box-shadow: 0 0 3px rgba(0, 0, 0, 0.1); */ /* 입체감 효과 (선택적) */
}

/* 탭 닫기 버튼 */
:deep(.el-tabs__item .el-icon) { /* ElIcon 컴포넌트 타겟 */
  margin-left: 8px; /* 텍스트와의 간격 */
  vertical-align: middle; /* 세로 중앙 정렬 */
  border-radius: 50%; /* 원형 배경 */
  padding: 1px; /* 클릭 영역 확보 */
  transition: all 0.3s cubic-bezier(.645,.045,.355,1);
}
/* 닫기 버튼 호버 시 */
:deep(.el-tabs__item .el-icon:hover) {
  background-color: var(--el-color-danger); /* 호버 배경색 (빨강) */
  color: white; /* 호버 아이콘 색 (흰색) */
}

/* 컨텍스트 메뉴 스타일 (이전과 동일) */
.contextmenu {
  margin: 0;
  background: #fff;
  z-index: 3000;
  position: absolute;
  list-style-type: none;
  padding: 5px 0;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 400;
  color: #333;
  box-shadow: 2px 2px 3px 0 rgba(0, 0, 0, .3);
}
.contextmenu li {
  margin: 0;
  padding: 7px 16px;
  cursor: pointer;
}
.contextmenu li:hover {
  background: #eee;
}
</style>
