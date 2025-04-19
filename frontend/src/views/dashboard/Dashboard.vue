<template>
  <div>
    <h2>대시보드</h2>
    <p>메인 컨텐츠 영역입니다. 이 페이지는 MainLayout 안에 표시됩니다.</p>
    <!-- TODO: 대시보드 내용 구현 -->
    <div>
      <!-- CSRF 테스트 버튼 추가 -->
      <el-button @click="runCsrfTest" :loading="testLoading">
        CSRF 보호 API 테스트 (POST)
      </el-button>
      <p v-if="testResultMessage">{{ testResultMessage }}</p>
    </div>
  </div>
</template>
<script setup lang="ts">
// 대시보드 관련 로직 추가
import {ref} from "vue";
import apiClient from "@/services/api.ts";
import type {ApiResponse} from "@/types/api.ts";
import type {MenuInfo} from "@/types/menu.ts";

const testLoading = ref(false);
const testResultMessage = ref<string | null>(null);

const runCsrfTest = async () => {
  testLoading.value = true;
  testResultMessage.value = null;
  console.log('Running CSRF test...');

  try {
    const response = await apiClient.post<ApiResponse<String>>('/api/user/csrf', {});

    if (response.status === 200 && response.data?.success) {
      console.log('CSRF Test API Response:', response.data);
      testResultMessage.value = `성공: ${response.data.message} (Data: ${response.data.data})`;
    } else {
      throw new Error(response.data?.message || '테스트 API 응답 오류');
    }

  } catch (error: any) {
    console.error('CSRF Test failed:', error);
    if (error.response) {
      testResultMessage.value = `실패: ${error.response.status} - ${error.response.data?.message || error.message}`;
    } else {
      testResultMessage.value = `실패: ${error.message}`;
    }
  } finally {
    testLoading.value = false;
  }
}
</script>
