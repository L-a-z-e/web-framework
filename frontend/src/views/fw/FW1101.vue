<template>
  <div class="menu-management-page">
    <!-- 1. 검색 영역 -->
    <SearchForm
      :search-fields="searchFields"
      :initial-values="searchParams"
      @search="handleSearch"
      @reset="handleReset"
      ref="searchFormRef"
      class="search-area"
    />

    <!-- 2. 메인 영역 (그리드 + 상세 폼) -->
    <el-row :gutter="20">
      <!-- 2.1 그리드 영역 -->
      <el-col :span="16">
        <el-card shadow="never">
          <Grid
            :columnDefs="columnDefs"
            :rowData="gridData"
            :height="'calc(100vh - 260px)'"
          :pagination="true"
          :paginationPageSize="10"
          @grid-ready="onGridReady"
          @row-selected="handleRowSelect"
          class="ag-theme-alpine"
          style="width: 100%;"
          >
          <template #toolbar-left>
            <span class="grid-title">[FW9110] 메뉴 목록</span>
          </template>
          <template #toolbar-right>
            <el-button size="small" :icon="Download" @click="handleExcelExport">엑셀다운로드</el-button>
            <el-button type="primary" size="small" :icon="Plus" @click="handleNew">신규 등록</el-button>
          </template>
          </Grid>
        </el-card>
      </el-col>

      <!-- 2.2 상세 정보/등록 폼 영역 -->
      <el-col :span="8">
        <el-card shadow="never">
          <div class="form-toolbar">
            <span class="form-title">등록/상세정보</span>
            <div class="button-group">
              <el-button size="small" :icon="Refresh" @click="resetDetailForm">초기화</el-button>
              <el-button type="primary" size="small" :icon="Check" @click="handleSave" :loading="saveLoading">저장</el-button>
            </div>
          </div>
          <el-form
            ref="detailFormRef"
            :model="detailForm"
            :rules="detailFormRules"
            label-position="top"
            label-width="100px"
            style="margin-top: 15px;"
            :disabled="!isDetailEditable"
            size="small"
          >
            <el-form-item label="업무구분" prop="bizDvcd"> <!-- Prop 이름 확인 필요 -->
              <el-select v-model="detailForm.bizDvcd" placeholder="선택">
                <el-option label="프레임워크(FW)" value="FW"></el-option>
                <el-option label="공통(CO)" value="CO"></el-option>
                <el-option label="샘플(SP)" value="SP"></el-option>
                <!-- TODO: 공통 코드 연동 -->
              </el-select>
            </el-form-item>
            <el-form-item label="메뉴ID" prop="menuId">
              <el-input v-model="detailForm.menuId" :disabled="isEditing" />
            </el-form-item>
            <el-form-item label="메뉴명" prop="menuNm">
              <el-input v-model="detailForm.menuNm" />
            </el-form-item>
            <el-form-item label="상위메뉴ID" prop="hrnMenuId">
              <!-- TODO: 메뉴 검색 팝업 연동 -->
              <el-input v-model="detailForm.hrnMenuId">
                <template #append><el-button :icon="Search" /></template>
              </el-input>
            </el-form-item>
            <el-form-item label="메뉴레벨" prop="menuLev">
              <el-select v-model="detailForm.menuLev" placeholder="선택">
                <el-option label="1레벨" :value="1"></el-option>
                <el-option label="2레벨" :value="2"></el-option>
                <el-option label="3레벨" :value="3"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="메뉴순서" prop="menuOrd">
              <el-input-number v-model="detailForm.menuOrd" :min="0" controls-position="right" />
            </el-form-item>
            <el-form-item label="메뉴아이콘" prop="menuIcon">
              <el-input v-model="detailForm.menuIcon" placeholder="Element Plus 아이콘 이름" />
            </el-form-item>
            <el-form-item label="사용여부" prop="useYn">
              <el-radio-group v-model="detailForm.useYn">
                <el-radio value="Y">예</el-radio>
                <el-radio value="N">아니오</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item label="필요권한" prop="authCd">
              <el-input v-model="detailForm.authCd" placeholder="예: ROLE_ADMIN" />
            </el-form-item>
            <el-form-item label="비고" prop="rmk">
              <el-input type="textarea" v-model="detailForm.rmk" :rows="3" />
            </el-form-item>
            <!-- TODO: 등록자, 수정자 등 추가 정보 표시 (읽기 전용) -->
          </el-form>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue';
import SearchForm from '@/components/SearchForm.vue';

import Grid from '@/components/Grid.vue'
import type { ColDef, GridReadyEvent, RowSelectedEvent, GridApi } from 'ag-grid-community';

import { ElCard, ElRow, ElCol, ElButton, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElRadioGroup, ElRadio, ElInputNumber, ElMessage } from 'element-plus';
import { Search, Refresh, Download, Plus, Check } from '@element-plus/icons-vue';
import type { FormInstance, FormRules } from 'element-plus';
// import { fetchMenuListApi, saveMenuApi } from '@/services/menu';
import type { MenuInfo } from '@/types/menu';

// --- 검색 영역 ---
const searchFormRef = ref<InstanceType<typeof SearchForm> | null>(null);
const searchFields = reactive([
  { key: 'bizDvcd', label: '업무구분', type: 'select', options: [/*...*/] },
  { key: 'menuNm', label: '메뉴명', type: 'text' },
]);
const searchParams = reactive<{ bizDvcd: string | null; menuNm: string }>({ bizDvcd: null, menuNm: '' });

// --- 그리드 영역 ---
// --- GridApi 타입 명시 ---
const gridApi = ref<GridApi>();
const gridData = ref<MenuInfo[]>([]);
const gridLoading = ref(false);

// --- AG Grid 컬럼 정의 (ColDef[]) ---
const columnDefs = ref<ColDef[]>([
  { headerName: '메뉴ID', field: 'menuId', width: 120, sortable: true, filter: true, },
  { headerName: '메뉴명', field: 'menuNm', width: 200, sortable: true, filter: true },
  { headerName: '상위메뉴ID', field: 'hrnMenuId', width: 120 },
  { headerName: '레벨', field: 'menuLev', width: 80, filter: 'agNumberColumnFilter' },
  { headerName: '순서', field: 'menuOrd', width: 80, sortable: true },
  { headerName: '아이콘', field: 'menuIcon', width: 120 },
  { headerName: '사용여부', field: 'useYn', width: 90, cellRenderer: (params: any) => params.value === 'Y' ? '예' : '아니오', filter: true },
  { headerName: '필요권한', field: 'authCd', width: 150, filter: true },
]);

// --- GridReady 이벤트 핸들러 ---
const onGridReady = (params: GridReadyEvent) => {
  gridApi.value = params.api;
};

// --- 상세 폼 영역 ---
const detailFormRef = ref<FormInstance>();
const detailForm = reactive<Partial<MenuInfo>>({});
const detailFormRules = reactive<FormRules>({ /* ... rules ... */ });
const isDetailEditable = ref(false);
const isEditing = ref(false);
const saveLoading = ref(false);

// --- 함수: 데이터 조회 ---
const handleSearch = async (params?: Record<string, any>) => {
  gridLoading.value = true;
  const currentParams = params || searchParams;
  console.log('Searching menus with params:', currentParams);
  try {
    // TODO: 실제 API 호출
    // const response = await fetchMenuListApi(currentParams);
    // gridData.value = response.data;
    // --- !!! 임시 데이터 타입 확인 (MenuInfo 에 맞게) !!! ---
    gridData.value = [
      { menuId: '/menus', menuNm: '메뉴 관리', hrnMenuId: '#system_group', menuOrd: 1, menuLev: 2, menuIcon: null, useYn: 'Y', authCd: 'ROLE_ADMIN', bizDvcd: "FW"}, // <<< OK: MenuInfo 타입 가정
      { menuId: '/sample', menuNm: '샘플', hrnMenuId: null, menuOrd: 2, menuLev: 1, menuIcon: 'Document', useYn: 'Y', authCd: 'ROLE_USER', bizDvcd: "FW"}, // <<< OK: MenuInfo 타입 가정
    ];
    // --- OK: 그리드 선택 초기화 ---
    gridApi.value?.deselectAll(); // AG Grid API 사용
    resetDetailForm();
  } catch (error) { /* ... */ }
  finally { gridLoading.value = false; }
};

// --- 함수: 검색 조건 초기화 (OK) ---
const handleReset = () => { /* ... */ };

// --- 함수: 그리드 행 선택 ---
// --- OK: 받는 파라미터 타입 수정 (AG Grid 이벤트) ---
const handleRowSelect = (event: RowSelectedEvent) => {
  const selectedNode = event.node;
  if (selectedNode && selectedNode.isSelected()) {
    // --- OK: 선택된 데이터는 MenuInfo 타입 ---
    const selectedData: MenuInfo = selectedNode.data;
    console.log('Row selected:', selectedData);
    resetDetailForm();
    // --- OK: MenuInfo -> MenuInfo 로 복사 ---
    Object.assign(detailForm, selectedData);
    // --- TODO: MenuInfo 에만 있는 필드 처리 (예: bizDvcd) ---
    // detailForm.bizDvcd = findBizDvcd(selectedData.menuId);

    isEditing.value = true;
    isDetailEditable.value = true;
  } else {
    // --- OK: 선택 해제 시 초기화 ---
    if (!gridApi.value?.getSelectedNodes().length) {
      resetDetailForm();
    }
  }
};

// --- 함수: 신규 등록 (OK) ---
const handleNew = () => {
  resetDetailForm();
  isEditing.value = false;
  isDetailEditable.value = true;
  detailForm.useYn = 'Y'; // OK: MenuInfo 타입 사용
};

// --- 함수: 상세 폼 초기화 ---
const resetDetailForm = () => {
  // --- OK: MenuInfo 타입 사용 ---
  Object.keys(detailForm).forEach(key => delete detailForm[key as keyof MenuInfo]);
  detailFormRef.value?.resetFields();
  isEditing.value = false;
  isDetailEditable.value = false;
};

// --- 함수: 저장 ---
const handleSave = async () => {
  if (!detailFormRef.value || !isDetailEditable.value) return;
  saveLoading.value = true;
  try {
    // --- OK: await validate() 사용 ---
    const isValid = await detailFormRef.value.validate();
    if (isValid) {
      console.log('Saving menu:', detailForm); // detailForm 은 MenuInfo 타입
      // TODO: 실제 API 호출 (detailForm 전달)
      await new Promise(resolve => setTimeout(resolve, 1000));
      ElMessage.success('저장되었습니다.');
      resetDetailForm();
      await handleSearch(); // OK
    } else {
      console.log('Detail form validation failed');
      // --- WARN: 유효성 실패 시 로딩 종료 누락 ---
      saveLoading.value = false; // 추가 필요
    }
  } catch (error) {
    console.error('Error saving menu:', error);
    ElMessage.error('저장 중 오류가 발생했습니다.');
  } finally {
    saveLoading.value = false;
  }
};

// --- 함수: 엑셀 다운로드 (OK: Grid API 사용) ---
const handleExcelExport = () => {
  if (gridApi.value) {
    gridApi.value.exportDataAsExcel({ fileName: '메뉴목록.xlsx'});
  } else {
    ElMessage.warning('그리드가 준비되지 않았습니다.');
  }
};

// --- 컴포넌트 마운트 시 초기 데이터 조회 ---
onMounted(() => {
  handleSearch();
});

</script>
<style scoped>
.menu-management-page {
  padding: 10px; /* 페이지 전체 여백 */
}

.search-area {
  margin-bottom: 20px; /* 검색 영역 아래 간격 */
}

/* 카드 헤더 스타일 (그리드, 폼 공통) */
.grid-toolbar,
.form-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px; /* 툴바 아래 간격 */
  padding-bottom: 10px; /* 툴바 아래 구분선 느낌 */
  border-bottom: 1px solid #eee;
}

.grid-title,
.form-title {
  font-size: 16px;
  font-weight: bold;
}

.button-group .el-button {
  margin-left: 8px;
}

/* 상세 폼 너비 고정 (선택 사항) */
/* .el-col:last-child .el-card {
  min-width: 400px;
} */

/* 상세 폼 내부 아이템 간격 등 */
.el-form--label-top .el-form-item {
  margin-bottom: 15px;
}
.el-form .el-select,
.el-form .el-input-number {
  width: 100%; /* Select, InputNumber 너비 100% */
}

/* 입력 비활성화 시 스타일 (더 명확하게) */
.el-form.is-disabled :deep(.el-input__inner),
.el-form.is-disabled :deep(.el-select__wrapper),
.el-form.is-disabled :deep(.el-input-number),
.el-form.is-disabled :deep(.el-textarea__inner) {
  background-color: #f5f7fa;
  cursor: not-allowed;
}
</style>
