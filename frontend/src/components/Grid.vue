<template>
  <div class="ag-grid-wrapper">
    <!-- 툴바 영역 (슬롯 사용) -->
    <div class="grid-toolbar" v-if="hasToolbarSlot">
      <div class="toolbar-left">
        <slot name="toolbar-left"></slot>
      </div>
      <div class="toolbar-right">
        <slot name="toolbar-right">
          <!-- 기본 엑셀 내보내기 버튼 -->
          <el-button size="small" :icon="Download" @click="exportToExcel" v-if="showExcelExport">
            엑셀 다운로드
          </el-button>
        </slot>
      </div>
    </div>

    <!-- AG-Grid 컴포넌트 -->
    <ag-grid-vue
      :style="{ height: height, width: '100%' }"
      :class="gridTheme"
      :rowData="rowData"
      :columnDefs="columnDefs"
      :gridOptions="mergedGridOptions"
      :pagination="gridPagination"
      :paginationPageSize="paginationPageSize"
      @grid-ready="onGridReady"
      @sort-changed="onSortChanged"
      @filter-changed="onFilterChanged"
      @pagination-changed="onPaginationChanged"
      @cell-clicked="onCellClicked"
      @row-selected="onRowSelected"
      @selection-changed="onSelectionChanged"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, useSlots, defineProps, defineEmits, watch } from 'vue';
import { AgGridVue } from 'ag-grid-vue3'
import type { GridOptions, GridReadyEvent, ColDef, SortChangedEvent, FilterChangedEvent, PaginationChangedEvent, CellClickedEvent, RowSelectedEvent, SelectionChangedEvent, GridApi } from 'ag-grid-community'; // 타입 임포트

// --- 기본 AG-Grid CSS 임포트 ---
import 'ag-grid-community/styles/ag-theme-alpine.css';
// import 'ag-grid-community/styles/ag-theme-balham.css';
// import 'ag-grid-community/styles/ag-theme-material.css';

// --- Element Plus 아이콘 임포트 (엑셀 버튼용) ---
import { ElButton } from 'element-plus';
import { Download } from '@element-plus/icons-vue';

// --- Props 정의 ---
const props = defineProps({
  rowData: { type: Array as () => any[], required: true },
  columnDefs: { type: Array as () => ColDef[], required: true },
  gridOptions: { type: Object as () => GridOptions, default: () => ({}) },
  height: { type: String, default: '400px' },
  gridTheme: { type: String, default: 'ag-theme-alpine' },
  pagination: { type: Boolean, default: true },
  paginationPageSize: { type: Number, default: 20 },
  showExcelExport: { type: Boolean, default: true },
  excelExportFileName: { type: String, default: 'grid_data' },
  rowSelectionType: { type: String as () => 'single' | 'multiple', default: 'single' }
});

// --- Events 정의 ---
const emit = defineEmits<{
  (e: 'grid-ready', params: GridReadyEvent): void
  (e: 'sort-changed', params: SortChangedEvent): void
  (e: 'filter-changed', params: FilterChangedEvent): void
  (e: 'pagination-changed', params: PaginationChangedEvent): void
  (e: 'cell-clicked', params: CellClickedEvent): void
  (e: 'row-selected', params: RowSelectedEvent): void
  (e: 'selection-changed', params: SelectionChangedEvent): void
}>();

// --- 내부 상태 ---
const gridApi = ref<GridApi | null>(null);
const slots = useSlots(); // 슬롯 사용 여부 확인용

// --- Grid Options 설정 ---
// 기본 Grid 옵션 정의
const defaultGridOptions = computed<GridOptions>(() => ({
  defaultColDef: {
    sortable: true,
    resizable: true,
    filter: true, // 기본 텍스트/숫자 필터 활성화
    minWidth: 50,
  },
  rowSelection: 'single', // 또는 'single' (다중 선택 필요시 'multiple')
  enableCellTextSelection: true, // 그리드 텍스트 선택 가능 (선택적)
}));

// Prop으로 받은 gridOptions 와 기본 옵션 병합
const mergedGridOptions = computed<GridOptions>(() => {
  const options: GridOptions = { // 명시적 타입 지정
    ...defaultGridOptions.value,
    paginationPageSizeSelector: false,
    rowSelection: props.rowSelectionType,
    ...props.gridOptions,
  };
  if (options.rowSelection === 'multiple') {
    // 다중 선택일 때만 헤더 체크박스 관련 로직 고려 (Enterprise)
  }
  return options;
});

const gridPagination = computed(() => props.pagination);

// --- 이벤트 핸들러 ---
const onGridReady = (params: GridReadyEvent) => {
  console.log('AG Grid is ready');
  gridApi.value = params.api;
  emit('grid-ready', params);
  // 그리드 너비에 맞게 컬럼 크기 자동 조절 (선택 사항)
  // params.api.sizeColumnsToFit();
};

const onSortChanged = (params: SortChangedEvent) => emit('sort-changed', params);
const onFilterChanged = (params: FilterChangedEvent) => emit('filter-changed', params);
const onPaginationChanged = (params: PaginationChangedEvent) => emit('pagination-changed', params);
const onCellClicked = (params: CellClickedEvent) => emit('cell-clicked', params);
const onRowSelected = (params: RowSelectedEvent) => emit('row-selected', params);
const onSelectionChanged = (params: SelectionChangedEvent) => emit('selection-changed', params);

// --- 기능 함수 ---
const exportToExcel = () => {
  if (gridApi.value) {
    gridApi.value.exportDataAsExcel({
      fileName: `${props.excelExportFileName}.xlsx`,
      // processCellCallback: (params) => { ... } // 데이터 가공 필요시
    });
  } else {
    console.error('Grid API not available for excel export.');
  }
};

// 툴바 슬롯 존재 여부 확인 (v-if 에서 사용)
const hasToolbarSlot = computed(() => !!slots['toolbar-left'] || !!slots['toolbar-right']);

// rowData 변경 감지 및 그리드 업데이트 (선택 사항)
// watch(() => props.rowData, (newData) => {
//   if (gridApi.value) {
//     gridApi.value.setRowData(newData);
//   }
// });

</script>

<style scoped>
.ag-grid-wrapper {
  width: 100%;
}
.grid-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.toolbar-left {
  /* 왼쪽 툴바 요소 스타일 */
}
.toolbar-right {
  /* 오른쪽 툴바 요소 스타일 */
  display: flex;
  gap: 8px; /* 버튼 간 간격 */
}
</style>
