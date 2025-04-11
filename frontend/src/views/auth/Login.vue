<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>로그인</span>
        </div>
      </template>
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0px" @keyup.enter="handleLogin">
        <el-form-item prop="empId">
          <el-input
            v-model="loginForm.empId"
            placeholder="Id"
            :prefix-icon="User"
            clearable
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="Password"
            show-password
            :prefix-icon="Lock"
            clearable
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%;">
            로그인
          </el-button>
        </el-form-item>
        <!-- 에러 메시지 표시 -->
        <el-alert v-if="errorMessage" :title="errorMessage" type="error" show-icon :closable="false" />
      </el-form>
      <!-- TODO: 아이디 찾기, 비밀번호 찾기 등 링크 추가 -->
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue';
import { useRouter } from 'vue-router';
import { ElCard, ElForm, ElFormItem, ElInput, ElButton, ElAlert, type FormInstance, type FormRules } from 'element-plus';
import { User, Lock } from '@element-plus/icons-vue';
import { useAuthStore } from '@/store';
import { loginApi, getMeApi } from '@/services/auth';
import type {UserInfo} from "@/types/user.ts";

const router = useRouter();
const authStore = useAuthStore();

const loginFormRef = ref<FormInstance>();
const loginForm = reactive({
  empId: '',
  password: '',
});
const loading = ref(false);
const errorMessage = ref<string | null>(null);

// 폼 유효성 검사 규칙
const loginRules = reactive<FormRules>({
  empId: [{ required: true, message: '아이디를 입력해주세요.', trigger: 'blur' }],
  password: [{ required: true, message: '비밀번호를 입력해주세요.', trigger: 'blur' }],
});

// 로그인 버튼 클릭 핸들러
const handleLogin = async () => {
  if (!loginFormRef.value)
    return;

  const isValid = await loginFormRef.value.validate().catch(() => false);

  if (!isValid)
    return;

  loading.value = true;
  errorMessage.value = null;

  try {
    // 1. 로그인 요청 보내기
    const loginResponse = await loginApi(loginForm); // POST /perform_login

    // 2. 로그인 성공 여부 확인 (200 OK 및 success 필드 확인)
    if (loginResponse.status === 200 && loginResponse.data?.success && loginResponse.data?.data) {
      // --- 로그인 성공 처리 ---
      const userInfo: UserInfo = loginResponse.data.data; // ApiResponse 의 data 필드 사용
      authStore.setUserInfo(userInfo); // 스토어에 사용자 정보 저장
      localStorage.setItem('tempLoggedIn', 'true'); // 임시
      router.push('/'); // 대시보드 이동
    } else {
      // 로그인 실패 (API 응답에서 success: false 또는 data 없음)
      throw new Error(loginResponse.data?.message || '로그인 정보가 올바르지 않습니다.');
    }

  } catch (error: any) {
    // Axios 오류 또는 위에서 던진 에러 처리
    console.error('Login process failed:', error);
    // 서버에서 보낸 실패 응답 메시지 사용 시도
    errorMessage.value = error.response?.data?.message || error.message || '로그인 처리 중 오류가 발생했습니다.';
    await authStore.logout();
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f0f2f5; /* 배경색 */
}
.login-card {
  width: 400px;
}
.card-header {
  text-align: center;
  font-size: 20px;
  font-weight: bold;
}
.el-alert { margin-top: 15px; }
</style>
