<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <span>로그인</span>
        </div>
      </template>
      <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="0px" @keyup.enter="handleLogin">
        <el-form-item prop="cmpCd">
          <el-input
            v-model="loginForm.cmpCd"
            placeholder="Company Code"
            :prefix-icon="OfficeBuilding"
          clearable
          />
        </el-form-item>
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
import { User, Lock, OfficeBuilding } from '@element-plus/icons-vue';
import { useAuthStore } from '@/store';
import { useMenuStore } from '@/store';
import { loginApi, getMeApi } from '@/services/auth';
import type {UserInfo} from "@/types/user.ts";

const router = useRouter();
const authStore = useAuthStore();
const menuStore = useMenuStore();

const loginFormRef = ref<FormInstance>();
const loginForm = reactive({
  cmpCd: '',
  empId: '',
  password: '',
});
const loading = ref(false);
const errorMessage = ref<string | null>(null);

// 폼 유효성 검사 규칙
const loginRules = reactive<FormRules>({
  cmpCd: [{ required: true, message: '회사 코드를 입력해주세요.', trigger: 'blur' }],
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
    const responsedata = await loginApi(loginForm); // <<< loginApi 호출
    const response = responsedata.data;

    // 2. 성공 처리 (HTTP 200 OK 가정, 인터셉터에서 success:false 는 에러로 처리됨)
    const userInfo: UserInfo = response.data;
    authStore.setUserInfo(userInfo); // 스토어에 사용자 정보 저장

    await menuStore.fetchMenus();      // 메뉴 정보 로드
    router.push('/');                 // 대시보드 이동

  } catch (error: any) {
    // 3. 실패 처리 (Axios 오류 또는 인터셉터에서 reject 된 API 레벨 에러)
    console.error('Login process failed:', error);
    // 인터셉터에서 reject 시 error 객체에 code 와 message 가 포함될 수 있음
    errorMessage.value = error?.response?.data?.message // 서버가 보낸 에러 메시지 우선 사용
      || error?.message                 // 인터셉터/Axios 에러 메시지
      || '로그인 처리 중 오류가 발생했습니다.'; // 기본 메시지
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
