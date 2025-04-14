<template>
  <div id="tags-view-container" class="tags-view-container">
    <el-scrollbar ref="scrollContainerRef" class="tags-view-wrapper" @wheel.prevent="handleScroll">
      <router-link
        v-for="tag in visitedViews"
        :key="tag.fullPath"
        :ref="el => setTagRef(el, tag)"
        :class="isActive(tag) ? 'active' : ''"
        :to="{ path: tag.path, query: tag.query }"
        class="tags-view-item"
        @click.middle.prevent="!isAffix(tag) ? closeSelectedTag(tag) : ''"
        @contextmenu.prevent="openMenu(tag, $event)"
      >
        {{ tag.title }}
        <!-- 고정되지 않은 탭에만 닫기 버튼 표시 -->
        <el-icon v-if="!isAffix(tag)" class="el-icon-close" @click.prevent.stop="closeSelectedTag(tag)">
          <Close />
        </el-icon>
      </router-link>
    </el-scrollbar>
    <!-- 우클릭 컨텍스트 메뉴 -->
    <ul v-show="visible" :style="{ left: left + 'px', top: top + 'px' }" class="contextmenu">
      <li @click="refreshSelectedTag(selectedTag)">Refresh</li>
      <li v-if="!isAffix(selectedTag)" @click="closeSelectedTag(selectedTag)">Close</li>
      <li @click="closeOthersTags">Close Others</li>
      <li @click="closeAllTags(selectedTag)">Close All</li>
    </ul>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, nextTick } from 'vue';
import { useRoute, useRouter, RouterLink } from 'vue-router';
import { useTagsViewStore } from '@/store/modules/tagsView'; // 스토어 임포트
import type { TagView } from '@/types/tagsView';
import { ElScrollbar, ElIcon } from 'element-plus';
import { Close } from '@element-plus/icons-vue';

// 스토어, 라우터, 현재 라우트 인스턴스 가져오기
const store = useTagsViewStore();
const router = useRouter();
const route = useRoute();

// 스토어 상태를 반응형으로 가져오기
const visitedViews = computed(() => store.visitedViews);

// 컨텍스트 메뉴 관련 상태
const visible = ref(false); // 메뉴 표시 여부
const top = ref(0);       // 메뉴 top 위치
const left = ref(0);      // 메뉴 left 위치
const selectedTag = ref<TagView>({}); // 우클릭된 탭 정보

// 스크롤 컨테이너 및 개별 탭 요소 참조
const scrollContainerRef = ref<InstanceType<typeof ElScrollbar>>();
const tagRefs = ref<Map<string, any>>(new Map()); // Map을 사용하여 각 탭의 DOM 요소 저장

/**
 * v-for 내부에서 각 router-link 요소의 참조를 설정/해제합니다.
 * @param el 참조된 요소 또는 null
 * @param tag 해당 요소에 연결된 탭 정보
 */
const setTagRef = (el: any, tag: TagView) => {
  if (tag.fullPath) { // fullPath가 있는 유효한 태그만 처리
    if (el) {
      tagRefs.value.set(tag.fullPath, el); // Map에 참조 추가
    } else {
      tagRefs.value.delete(tag.fullPath); // Map에서 참조 제거 (컴포넌트 언마운트 시)
    }
  }
};

/** 현재 라우트와 탭의 경로가 일치하는지 확인하여 활성 상태 결정 */
const isActive = (tag: TagView): boolean => tag.path === route.path;

/** 탭이 고정 탭인지 확인 (meta.affix) */
const isAffix = (tag: TagView): boolean => tag.meta?.affix === true;

/**
 * 활성화된 탭으로 스크롤을 이동시킵니다.
 */
const moveToCurrentTag = () => {
  nextTick(() => { // DOM 업데이트 후 실행 보장
    const currentTagEl = tagRefs.value.get(route.fullPath); // 현재 활성 탭의 DOM 요소 찾기
    if (currentTagEl && scrollContainerRef.value) {
      const container = scrollContainerRef.value.$el as HTMLElement;
      const containerWidth = container.offsetWidth;
      const scrollWrapper = scrollContainerRef.value.wrapRef; // 스크롤 가능한 내부 요소
      if (!scrollWrapper) return;

      // 스크롤 위치 계산 (개선된 방식)
      const tagOffsetLeft = currentTagEl.offsetLeft || 0; // 요소의 왼쪽 옵셋
      const tagWidth = currentTagEl.offsetWidth || 0;   // 요소의 너비
      const currentScrollLeft = scrollWrapper.scrollLeft; // 현재 스크롤 위치

      // 탭이 왼쪽에 가려진 경우
      if (tagOffsetLeft < currentScrollLeft) {
        scrollWrapper.scrollTo({ left: tagOffsetLeft, behavior: 'smooth' });
      }
      // 탭이 오른쪽에 가려진 경우
      else if (tagOffsetLeft + tagWidth > currentScrollLeft + containerWidth) {
        // 오른쪽 끝으로 스크롤 (약간의 여백 고려 가능)
        scrollWrapper.scrollTo({ left: tagOffsetLeft + tagWidth - containerWidth, behavior: 'smooth' });
      }
    }
  });
};

/**
 * 선택된 탭을 닫습니다. (스토어 액션 호출)
 * @param view 닫을 탭 정보
 */
const closeSelectedTag = (view: TagView) => {
  if (isAffix(view)) return; // 고정 탭은 닫지 않음
  store.delView(view).then(() => {
    // 닫힌 탭이 현재 활성 탭이었다면, 남은 탭 중 마지막 탭으로 이동
    if (isActive(view)) {
      store.toLastView(view);
    }
  });
};

/**
 * 컨텍스트 메뉴에서 'Close Others' 클릭 시 호출됩니다. (스토어 액션 호출)
 */
const closeOthersTags = () => {
  // 우클릭된 탭(selectedTag) 정보가 유효한지 확인
  if (!selectedTag.value.fullPath) return;
  // 선택된 탭으로 먼저 이동 (사용자 경험)
  router.push(selectedTag.value); // 라우트 객체 직접 전달 가능
  // 스토어 액션 호출하여 다른 탭 제거
  store.delOthersViews(selectedTag.value).then(() => {
    moveToCurrentTag(); // 스크롤 위치 조정
  });
};

/**
 * 컨텍스트 메뉴에서 'Close All' 클릭 시 호출됩니다. (스토어 액션 호출)
 * @param view 현재 컨텍스트 메뉴가 열린 탭 정보 (이동 기준)
 */
const closeAllTags = (view: TagView) => {
  store.delAllViews().then(() => {
    // 모든 탭 (고정 탭 제외) 제거 후 남은 탭 또는 기본 경로로 이동
    store.toLastView(view);
  });
};

/**
 * 탭에서 우클릭 시 컨텍스트 메뉴를 엽니다.
 * @param tag 우클릭된 탭 정보
 * @param e MouseEvent 객체
 */
const openMenu = (tag: TagView, e: MouseEvent) => {
  const menuMinWidth = 105; // 컨텍스트 메뉴 최소 너비
  const container = document.getElementById('tags-view-container'); // TagsView 컨테이너 요소
  if (!container) return;

  const offsetLeft = container.getBoundingClientRect().left; // 컨테이너의 화면 왼쪽 기준 위치
  const { clientWidth } = container;                       // 컨테이너 너비
  const maxLeft = clientWidth - menuMinWidth;              // 메뉴가 오른쪽으로 벗어나지 않도록 최대 left 위치 계산

  // 메뉴 left 위치 계산 (컨테이너 기준 + 여백)
  let calculatedLeft = e.clientX - offsetLeft + 15; // 15px 정도 여백
  if (calculatedLeft > maxLeft) {
    calculatedLeft = maxLeft; // 최대 위치 제한
  }

  // 메뉴 top 위치 계산 (헤더 높이 등 고려하여 조정 필요)
  let calculatedTop = e.clientY - 50; // 예: 헤더 높이 50px 가정, 실제 값으로 조정

  // 계산된 위치와 선택된 탭 정보 업데이트, 메뉴 표시
  left.value = calculatedLeft;
  top.value = calculatedTop;
  visible.value = true;
  selectedTag.value = tag;
};

/** 컨텍스트 메뉴를 닫습니다. */
const closeMenu = () => {
  visible.value = false;
};

/**
 * 컨텍스트 메뉴에서 'Refresh' 클릭 시 호출됩니다.
 * 선택된 탭의 컴포넌트를 새로고침(재렌더링)합니다.
 * @param view 새로고침할 탭 정보
 */
const refreshSelectedTag = (view: TagView) => {
  // 1. 스토어에서 해당 뷰의 캐시를 제거합니다. (keep-alive 캐시 해제)
  store.delCachedView(view);
  const { fullPath } = view; // 현재 탭의 전체 경로 저장

  // 2. nextTick을 사용하여 DOM 업데이트 주기를 기다린 후 실행합니다.
  nextTick(() => {
    // 3. 현재 경로와 동일한 경로로 replace 합니다.
    // Vue Router는 기본적으로 같은 경로로 이동 시 컴포넌트를 재사용하지만,
    // keep-alive 캐시가 제거되었으므로 컴포넌트가 재마운트되어 새로고침 효과가 나타납니다.
    // (만약 Redirect 뷰 방식을 사용한다면 해당 코드로 대체)
    router.replace({ path: fullPath }).catch(err => {
      // 동일 경로 이동 시 발생하는 NavigationDuplicated 에러는 무시해도 괜찮음
      console.warn('Router replace error (likely NavigationDuplicated):', err);
    });
  });
};


/**
 * 마우스 휠 이벤트를 처리하여 가로 스크롤을 수행합니다.
 * @param e WheelEvent 객체
 */
const handleScroll = (e: WheelEvent) => {
  const eventDelta = e.deltaY || e.detail; // 휠 이동량 (세로 스크롤 기준)
  const scrollWrapper = scrollContainerRef.value?.wrapRef; // 스크롤 가능한 내부 요소
  if (scrollWrapper) {
    // 세로 휠 이동량을 가로 스크롤 이동량으로 변환하여 적용
    scrollWrapper.scrollLeft += eventDelta / 4; // 스크롤 속도 조절 가능
  }
};

// --- Watchers ---

// 현재 라우트 변경을 감지하여 활성 탭으로 스크롤 이동
watch(route, () => {
  // addTags(); // 라우트 변경 시 탭 추가는 현재 afterEach 가드에서 처리 중
  moveToCurrentTag(); // 스크롤 위치만 조정
});

// 컨텍스트 메뉴 표시 상태 변경 감지 (외부 클릭 시 닫기 위함)
watch(visible, (value) => {
  if (value) {
    // 메뉴가 보일 때 body에 클릭 이벤트 리스너 추가
    document.body.addEventListener('click', closeMenu);
  } else {
    // 메뉴가 사라질 때 리스너 제거
    document.body.removeEventListener('click', closeMenu);
  }
});

// --- Lifecycle Hooks ---

// 컴포넌트 마운트 시 초기화 작업 수행
onMounted(() => {
  // initTags(); // 초기 고정 탭 추가 로직 (필요시 활성화, 스토어/가드와 연계)
  moveToCurrentTag(); // 현재 활성 탭으로 스크롤 이동
});

</script>

<style scoped>
.tags-view-container {
  height: 34px;
  width: 100%;
  background: #fff;
  border-bottom: 1px solid #d8dce5;
  box-shadow: 0 1px 3px 0 rgba(0, 0, 0, .12), 0 0 3px 0 rgba(0, 0, 0, .04);
}

.tags-view-container .tags-view-wrapper {
  height: 100%;
  position: relative;
}

.tags-view-container .tags-view-wrapper .tags-view-item {
  display: inline-block;
  position: relative;
  cursor: pointer;
  height: 26px;
  line-height: 26px;
  border: 1px solid #d8dce5;
  color: #495060;
  background: #fff;
  padding: 0 8px;
  font-size: 12px;
  margin-left: 5px;
  margin-top: 4px;
}

.tags-view-container .tags-view-wrapper .tags-view-item:first-of-type {
  margin-left: 15px;
}

.tags-view-container .tags-view-wrapper .tags-view-item:last-of-type {
  margin-right: 15px;
}

/* 활성 탭 스타일 */
.tags-view-container .tags-view-wrapper .tags-view-item.active {
  background-color: #42b983; /* 테마 색상에 맞게 수정 */
  color: #fff;
  border-color: #42b983;
}

/* 활성 탭 점 표시 */
.tags-view-container .tags-view-wrapper .tags-view-item.active::before {
  content: '';
  background: #fff;
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  position: relative;
  margin-right: 2px;
}

/* 닫기 아이콘 스타일 */
.tags-view-container .tags-view-wrapper .tags-view-item .el-icon-close {
  width: 16px;
  height: 16px;
  vertical-align: 2px;
  border-radius: 50%;
  text-align: center;
  transition: all .3s cubic-bezier(.645, .045, .355, 1);
  transform-origin: 100% 50%;
  margin-left: 5px;
}

/* 닫기 아이콘 내부 (크기 조정) */
.tags-view-container .tags-view-wrapper .tags-view-item .el-icon-close::before {
  /* ::before 선택자는 el-icon 내부 svg에 직접 적용되지 않을 수 있음. */
  /* 필요시 아이콘 크기 조정은 font-size 등으로 시도 */
  /* transform: scale(.6); */
  /* display: inline-block; */
  /* vertical-align: -3px; */
}

/* 닫기 아이콘 호버 */
.tags-view-container .tags-view-wrapper .tags-view-item .el-icon-close:hover {
  background-color: #b4bccc;
  color: #fff;
}


/* 컨텍스트 메뉴 스타일 */
.tags-view-container .contextmenu {
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

.tags-view-container .contextmenu li {
  margin: 0;
  padding: 7px 16px;
  cursor: pointer;
}

.tags-view-container .contextmenu li:hover {
  background: #eee;
}
</style>
