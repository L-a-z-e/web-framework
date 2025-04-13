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
      <el-col :span="16"> <!-- 그리드가 더 넓게 차지하도록 설정 (비율 조정 가능) -->
        <el-card shadow="never">
          <div class="grid-toolbar">
            <span class="grid-title">[FW9110] 메뉴 목록</span>
            <div class="button-group">
              <el-button size="small" :icon="Download" @click="handleExcelExport">엑셀다운로드</el-button>
              <el-button type="primary" size="small" :icon="Plus" @click="handleNew">신규 등록</el-button>
            </div>
          </div>
          <!-- 임시 ElTable 사용 (나중에 AgGrid 로 교체) -->
          <el-table
            :data="gridData"
            style="width: 100%; margin-top: 15px;"
            border
            size="small"
            highlight-current-row
            @current-change="handleRowSelect"
            v-loading="gridLoading"
          >
            <el-table-column prop="menuId" label="메뉴ID" width="100" sortable />
            <el-table-column prop="menuNm" label="메뉴명" width="180" sortable />
            <el-table-column prop="upperMenuId" label="상위메뉴ID" width="120" />
            <el-table-column prop="menuLvl" label="메뉴레벨" width="100" />
            <el-table-column prop="menuOrdr" label="메뉴순서" width="100" />
            <el-table-column prop="menuIcon" label="메뉴아이콘" width="120" />
            <el-table-column prop="useYn" label="사용여부" width="90">
              <template #default="scope">
                {{ scope.row.useYn === 'Y' ? '예' : '아니오' }}
              </template>
            </el-table-column>
            <el-table-column prop="authCd" label="필요권한" width="120" />
            <!-- 다른 필요한 컬럼 추가 -->
          </el-table>
          <!-- TODO: 페이지네이션 컴포넌트 추가 -->
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
            <el-form-item label="업무구분" prop="jobType"> <!-- Prop 이름 확인 필요 -->
              <el-select v-model="detailForm.jobType" placeholder="선택">
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
            <el-form-item label="상위메뉴ID" prop="upperMenuId">
              <!-- TODO: 메뉴 검색 팝업 연동 -->
              <el-input v-model="detailForm.upperMenuId">
                <template #append><el-button :icon="Search" /></template>
              </el-input>
            </el-form-item>
            <el-form-item label="메뉴레벨" prop="menuLvl">
              <el-select v-model="detailForm.menuLvl" placeholder="선택">
                <el-option label="1레벨" :value="1"></el-option>
                <el-option label="2레벨" :value="2"></el-option>
                <el-option label="3레벨" :value="3"></el-option>
              </el-select>
            </el-form-item>
            <el-form-item label="메뉴순서" prop="menuOrdr">
              <el-input-number v-model="detailForm.menuOrdr" :min="0" controls-position="right" />
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
            <el-form-item label="비고" prop="description">
              <el-input type="textarea" v-model="detailForm.description" :rows="3" />
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
import SearchForm from '@/components/SearchForm.vue'; // 공통 검색폼
// import AgGrid from '@/components/framework/AgGrid.vue'; // 나중에 사용할 그리드
import { ElTable, ElTableColumn, ElCard, ElRow, ElCol, ElButton, ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElDatePicker, ElRadioGroup, ElRadio, ElInputNumber, ElMessage } from 'element-plus'; // Element Plus 컴포넌트 임포트
import { Search, Refresh, Download, Plus, Check } from '@element-plus/icons-vue'; // 아이콘 임포트
import type { FormInstance, FormRules } from 'element-plus';
// import { fetchMenuListApi, saveMenuApi, deleteMenuApi } from '@/services/menu'; // <<< 실제 API 서비스 함수 임포트 필요
import type { MenuInfo } from '@/types/menu'; // 메뉴 타입 정의

// --- 검색 영역 ---
const searchFormRef = ref<InstanceType<typeof SearchForm> | null>(null);
const searchFields = reactive([
  { key: 'jobType', label: '업무구분', type: 'select', options: [{label: '전체', value: null}, {label: '프레임워크(FW)', value: 'FW'}, {label: '공통(CO)', value: 'CO'}, {label: '샘플(SP)', value: 'SP'}] },
  { key: 'menuNm', label: '메뉴명', type: 'text' },
]);
const searchParams = reactive({ jobType: null, menuNm: '' }); // 검색 조건 모델

// --- 그리드 영역 ---
const gridData = ref<MenuInfo[]>([]); // 그리드 데이터
const gridLoading = ref(false);

// --- 상세 폼 영역 ---
const detailFormRef = ref<FormInstance>(); // 상세 폼 참조
const detailForm = reactive<Partial<MenuInfo>>({}); // 상세 폼 모델 (Partial 로 빈 객체 허용)
const detailFormRules = reactive<FormRules>({ // 상세 폼 유효성 규칙
  menuId: [{ required: true, message: '메뉴ID는 필수입니다.', trigger: 'blur' }],
  menuNm: [{ required: true, message: '메뉴명은 필수입니다.', trigger: 'blur' }],
  menuLvl: [{ required: true, message: '메뉴레벨은 필수입니다.', trigger: 'change' }],
  menuOrdr: [{ required: true, message: '메뉴순서는 필수입니다.', trigger: 'blur' }],
  useYn: [{ required: true, message: '사용여부는 필수입니다.', trigger: 'change' }],
});
const isDetailEditable = ref(false); // 폼 활성화 여부
const isEditing = ref(false); // 수정 모드 여부 (ID 필드 비활성화 등)
const saveLoading = ref(false);

// --- 함수: 데이터 조회 ---
const handleSearch = async (params?: Record<string, any>) => {
  gridLoading.value = true;
  // 실제 검색 파라미터는 searchParams 또는 이벤트로 받은 params 사용
  const currentParams = params || searchParams;
  console.log('Searching menus with params:', currentParams);
  try {
    // TODO: 백엔드 API 호출 (fetchMenuListApi)
    // const response = await fetchMenuListApi(currentParams);
    // gridData.value = response.data; // API 응답 데이터로 그리드 업데이트 (페이징 고려 필요)
    // 임시 데이터
    gridData.value = [
      { menuId: '/menus', menuNm: '메뉴 관리', upperMenuId: '#system_group', menuOrdr: 1, menuLvl: 2, useYn: 'Y', authCd: 'ROLE_ADMIN'},
      { menuId: '/sample', menuNm: '샘플', upperMenuId: null, menuOrdr: 2, menuLvl: 1, useYn: 'Y', authCd: 'ROLE_USER'},
    ];
    resetDetailForm(); // 조회 후 상세 폼 초기화
  } catch (error) {
    console.error('Error fetching menus:', error);
    ElMessage.error('메뉴 목록 조회 중 오류가 발생했습니다.');
  } finally {
    gridLoading.value = false;
  }
};

// --- 함수: 검색 조건 초기화 ---
const handleReset = () => {
  // SearchForm 컴포넌트의 초기화 메소드 호출 (만약 직접 제어 필요 없다면 searchFormRef 불필요)
  searchFormRef.value?.resetForm();
  // 또는 searchParams 를 직접 초기화
  // searchParams.jobType = null;
  // searchParams.menuNm = '';
  handleSearch(searchParams); // 초기화 후 다시 조회
};

// --- 함수: 그리드 행 선택 ---
const handleRowSelect = (currentRow: MenuInfo | null) => {
  if (currentRow) {
    // 선택된 행의 데이터를 복사하여 상세 폼에 채움 (수정 모드)
    Object.assign(detailForm, currentRow);
    isEditing.value = true;
    isDetailEditable.value = true; // 폼 활성화
  } else {
    // 선택 해제 시 폼 비우기
    resetDetailForm();
  }
};

// --- 함수: 신규 등록 버튼 클릭 ---
const handleNew = () => {
  resetDetailForm(); // 폼 초기화
  isEditing.value = false; // 등록 모드
  isDetailEditable.value = true; // 폼 활성화
  // 필요시 기본값 설정
  detailForm.useYn = 'Y';
};

// --- 함수: 상세 폼 초기화 ---
const resetDetailForm = () => {
  // detailForm 의 모든 속성을 undefined 또는 기본값으로 초기화
  Object.keys(detailForm).forEach(key => delete detailForm[key as keyof MenuInfo]);
  detailFormRef.value?.resetFields(); // Element Plus 폼 초기화
  isEditing.value = false;
  isDetailEditable.value = false; // 폼 비활성화
};

// --- 함수: 저장 버튼 클릭 ---
const handleSave = async () => {
  if (!detailFormRef.value || !isDetailEditable.value) return;
  await detailFormRef.value.validate(async (valid) => {
    if (valid) {
      saveLoading.value = true;
      try {
        console.log('Saving menu:', detailForm);
        // TODO: 백엔드 API 호출 (saveMenuApi - isEditing 값으로 분기)
        // if (isEditing.value) { // 수정
        //   await saveMenuApi(detailForm.menuId, detailForm);
        // } else { // 등록
        //   await saveMenuApi(null, detailForm);
        // }
        ElMessage.success('저장되었습니다.');
        resetDetailForm(); // 저장 후 폼 초기화
        handleSearch(); // 그리드 재조회
      } catch (error) {
        console.error('Error saving menu:', error);
        ElMessage.error('저장 중 오류가 발생했습니다.');
      } finally {
        saveLoading.value = false;
      }
    } else {
      console.log('Detail form validation failed');
      return false;
    }
  });
};

// --- 함수: 엑셀 다운로드 ---
const handleExcelExport = () => {
  console.log('Excel export requested');
  // TODO: 그리드 데이터 엑셀 다운로드 로직 구현
  ElMessage.info('엑셀 다운로드 기능은 준비중입니다.');
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
