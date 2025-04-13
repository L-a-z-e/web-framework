<template>
  <el-form :model="searchParams" :inline="inline" label-width="auto" class="search-form" @submit.prevent="handleSearch">
    <!-- 동적 필드 생성 -->
    <el-form-item v-for="field in searchFields" :key="field.key" :label="field.label" class="search-form-item">
      <!-- 텍스트 입력 -->
      <el-input
        v-if="field.type === 'text' || !field.type"
        v-model="searchParams[field.key]"
        :placeholder="field.placeholder || field.label"
        clearable
        size="small"
      />
      <!-- 셀렉트 박스 -->
      <el-select
        v-else-if="field.type === 'select'"
        v-model="searchParams[field.key]"
        :placeholder="field.placeholder || field.label"
        clearable
        size="small"
      >
        <el-option
          v-for="option in field.options"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <!-- 날짜 범위 선택 -->
      <el-date-picker
        v-else-if="field.type === 'date-range'"
        v-model="searchParams[field.key]"
        type="daterange"
        range-separator="~"
        start-placeholder="시작일"
        end-placeholder="종료일"
        size="small"
        value-format="YYYY-MM-DD"
      />
      <!-- 체크박스 그룹 -->
      <el-checkbox-group
        v-else-if="field.type === 'checkbox'"
        v-model="searchParams[field.key]"
        size="small"
      >
        <el-checkbox
          v-for="option in field.options"
          :key="option.value"
          :label="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </el-checkbox>
      </el-checkbox-group>
      <!-- 라디오 버튼 그룹 -->
      <el-radio-group
        v-else-if="field.type === 'radio'"
        v-model="searchParams[field.key]"
        size="small"
      >
        <el-radio
          v-for="option in field.options"
          :key="option.value"
          :label="option.value"
          :value="option.value"
        >
          {{ option.label }}
        </el-radio>
      </el-radio-group>
    </el-form-item>

    <!-- 검색/초기화 버튼 영역 -->
    <el-form-item class="search-buttons">
      <el-button type="primary" @click="handleSearch" :icon="Search" size="small">조회</el-button>
      <el-button @click="handleReset" :icon="Refresh" size="small">초기화</el-button>
      <slot name="extra-buttons"></slot> <!-- 추가 버튼 위한 슬롯 -->
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch, defineProps, defineEmits, onMounted } from 'vue';
import {
  ElForm, ElFormItem, ElInput, ElSelect, ElOption, ElDatePicker, ElButton, ElIcon,
  ElCheckboxGroup, ElCheckbox, ElRadioGroup, ElRadio // Checkbox, Radio 추가
} from 'element-plus';
import { Search, Refresh } from '@element-plus/icons-vue';

// --- Props 정의 (SearchField 에 type 추가) ---
interface SearchFieldOption { label: string; value: any; }
interface SearchField {
  key: string;
  label: string;
  type?: 'text' | 'select' | 'date-range' | 'checkbox' | 'radio' | string;
  options?: SearchFieldOption[];
  placeholder?: string;
  defaultValue?: any;
}
const props = defineProps({
  searchFields: { type: Array as () => SearchField[], required: true },
  initialValues: { type: Object, default: () => ({}) },
  inline: { type: Boolean, default: true },
});

// --- Events 정의 (변경 없음) ---
const emit = defineEmits<{
  (e: 'search', params: Record<string, any>): void
  (e: 'reset'): void
}>();

// --- 내부 상태 (변경 없음) ---
const searchParams = reactive<Record<string, any>>({});

// --- 함수: 필드 타입별 기본값 (checkbox, radio 추가) ---
function getDefaultValue(type?: string): any {
  switch (type) {
    case 'select': return null;
    case 'date-range': return [];
    case 'checkbox': return []; // 체크박스는 빈 배열
    case 'radio': return null; // 라디오는 null 또는 첫번째 옵션 등
    case 'text':
    default: return '';
  }
}

// --- 함수: 검색 파라미터 초기화 (변경 없음) ---
function initializeSearchParams() {
  props.searchFields.forEach(field => {
    searchParams[field.key] = props.initialValues[field.key] ?? field.defaultValue ?? getDefaultValue(field.type);
  });
}

// --- 이벤트 핸들러: 검색 (변경 없음) ---
function handleSearch() {
  // date-range 값이 null 인 경우 빈 배열[] 이 아닌 null 이나 undefined 로 전달될 수 있으므로,
  // API 스펙에 맞게 전처리 필요 시 여기서 수행
  const paramsToEmit = { ...searchParams };
  // 예시: 날짜 범위가 빈 배열이면 키 자체를 제거 (선택 사항)
  // for (const key in paramsToEmit) {
  //   if (Array.isArray(paramsToEmit[key]) && paramsToEmit[key].length === 0) {
  //     delete paramsToEmit[key];
  //   }
  // }
  emit('search', paramsToEmit);
}

// --- 이벤트 핸들러: 초기화 (변경 없음) ---
function handleReset() {
  initializeSearchParams();
  emit('reset');
}

// --- 생명 주기 훅 (변경 없음) ---
onMounted(() => {
  initializeSearchParams();
});
watch(() => props.initialValues, () => initializeSearchParams(), { deep: true });

// --- 함수 노출 (변경 없음) ---
defineExpose({
  getSearchParams: () => ({ ...searchParams }),
  resetForm: handleReset
});
</script>

<style scoped>
/* 스타일은 이전과 유사하게 유지, 필요시 조정 */
.search-form {
  background-color: #f7f8fa;
  padding: 15px 20px 0px 20px; /* 아래 패딩 제거하여 버튼과 간격 조정 */
  border-radius: 4px;
  margin-bottom: 15px;
  /* box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04); */ /* 그림자 제거 또는 조정 */
}
.search-form .el-form-item {
  margin-bottom: 15px;
}
.search-form-item .el-input,
.search-form-item .el-select,
.search-form-item .el-date-editor,
.search-form-item .el-checkbox-group, /* 너비 지정 */
.search-form-item .el-radio-group {   /* 너비 지정 */
  width: 200px; /* 기본 너비, 필요시 field 설정으로 개별 조정 가능 */
}
.search-buttons {
  margin-left: auto; /* 버튼들을 오른쪽으로 밀착 (inline=true 가정) */
  padding-left: 10px; /* 왼쪽 버튼과의 최소 간격 */
}
/* Form 이 inline 일 때 버튼 정렬을 위해 추가 */
.el-form--inline .search-buttons {
  display: inline-block;
  vertical-align: top; /* 다른 폼 아이템과 수직 정렬 */
}
</style>
