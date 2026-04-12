<template>
  <div class="login-page">
    <section class="visual-panel" aria-hidden="true">
      <div class="visual-content">
        <div class="brand-block">
          <span class="brand-pill">实习管理平台</span>
          <h1>教师实习管理平台</h1>
        </div>
        <div class="visual-stage">
          <AnimatedCharacters
            :is-typing="isTyping"
            :show-password="currentPasswordVisible"
            :password-length="currentPasswordLength"
          />
        </div>
      </div>
    </section>

    <section class="form-panel">
      <div class="login-card">
        <div class="login-head">
          <h1>{{ isLoginMode ? '欢迎登录' : '注册申请' }}</h1>
          <p v-if="isLoginMode">请使用学号/工号和密码登录平台。</p>
          <p v-else>请如实填写注册信息，提交后由管理员审核。</p>
        </div>

        <el-form
          v-if="isLoginMode"
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          label-position="top"
          class="login-form"
          @submit.prevent
        >
          <el-form-item label="学号/工号" prop="account">
            <div class="input-wrap" :class="{ focused: fieldFocus.account }">
              <input
                v-model.trim="loginForm.account"
                type="text"
                placeholder="请输入学号/工号"
                @focus="fieldFocus.account = true; onTyping()"
                @blur="fieldFocus.account = false"
                @input="onTyping"
              />
            </div>
          </el-form-item>

          <el-form-item label="密码" prop="password">
            <div class="input-wrap" :class="{ focused: fieldFocus.password }">
              <input
                v-model="loginForm.password"
                :type="loginPasswordVisible ? 'text' : 'password'"
                placeholder="请输入登录密码"
                @focus="fieldFocus.password = true; onTyping()"
                @blur="fieldFocus.password = false"
                @input="onTyping"
              />
              <button type="button" class="password-toggle" @click="loginPasswordVisible = !loginPasswordVisible">
                <el-icon>
                  <component :is="loginPasswordVisible ? Hide : View" />
                </el-icon>
              </button>
            </div>
          </el-form-item>

          <div class="login-meta">
            <label class="remember-me">
              <input v-model="loginForm.rememberMe" type="checkbox" />
              <span>记住我</span>
            </label>
            <button type="button" class="forgot-link" @click="handleForgotPassword">忘记密码</button>
          </div>

          <button type="submit" class="login-button" :disabled="loginSubmitting" @click="handleLogin">
            {{ loginSubmitting ? '登录中...' : '登录' }}
          </button>
        </el-form>

        <el-form
          v-else
          ref="registerFormRef"
          :model="registerForm"
          :rules="registerRules"
          label-position="top"
          class="login-form"
          @submit.prevent
        >
          <div class="field-grid">
            <el-form-item label="学号/工号" prop="studentNo">
              <div class="input-wrap" :class="{ focused: fieldFocus.studentNo }">
                <input
                  v-model.trim="registerForm.studentNo"
                  type="text"
                  placeholder="请输入学号/工号"
                  @focus="fieldFocus.studentNo = true; onTyping()"
                  @blur="fieldFocus.studentNo = false"
                  @input="onTyping"
                />
              </div>
            </el-form-item>

            <el-form-item label="姓名" prop="realName">
              <div class="input-wrap" :class="{ focused: fieldFocus.realName }">
                <input
                  v-model.trim="registerForm.realName"
                  type="text"
                  placeholder="请输入姓名"
                  @focus="fieldFocus.realName = true; onTyping()"
                  @blur="fieldFocus.realName = false"
                  @input="onTyping"
                />
              </div>
            </el-form-item>
          </div>

          <div class="field-grid">
            <el-form-item label="密码" prop="password">
              <div class="input-wrap" :class="{ focused: fieldFocus.password }">
                <input
                  v-model="registerForm.password"
                  :type="registerPasswordVisible ? 'text' : 'password'"
                  placeholder="请输入登录密码"
                  @focus="fieldFocus.password = true; onTyping()"
                  @blur="fieldFocus.password = false"
                  @input="onTyping"
                />
                <button type="button" class="password-toggle" @click="registerPasswordVisible = !registerPasswordVisible">
                  <el-icon>
                    <component :is="registerPasswordVisible ? Hide : View" />
                  </el-icon>
                </button>
              </div>
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <div class="input-wrap" :class="{ focused: fieldFocus.confirmPassword }">
                <input
                  v-model="registerForm.confirmPassword"
                  :type="registerConfirmPasswordVisible ? 'text' : 'password'"
                  placeholder="请再次输入密码"
                  @focus="fieldFocus.confirmPassword = true; onTyping()"
                  @blur="fieldFocus.confirmPassword = false"
                  @input="onTyping"
                />
                <button
                  type="button"
                  class="password-toggle"
                  @click="registerConfirmPasswordVisible = !registerConfirmPasswordVisible"
                >
                  <el-icon>
                    <component :is="registerConfirmPasswordVisible ? Hide : View" />
                  </el-icon>
                </button>
              </div>
            </el-form-item>
          </div>

          <div class="field-grid field-grid--triple">
            <el-form-item label="院系" prop="deptId">
              <div class="input-wrap input-wrap--select" :class="{ focused: fieldFocus.deptId }">
                                <el-select
                  v-model="registerForm.deptId"
                  class="campus-select"
                  popper-class="campus-select-popper"
                  placeholder="请选择院系"
                  :teleported="true"
                  @focus="fieldFocus.deptId = true; onTyping()"
                  @blur="fieldFocus.deptId = false"
                  @change="onTyping"
                >
                  <el-option
                    v-for="item in departmentOptions"
                    :key="String(item.id)"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </div>
            </el-form-item>

            <el-form-item label="专业" prop="majorId">
              <div class="input-wrap input-wrap--select" :class="{ focused: fieldFocus.majorId }">
                                <el-select
                  v-model="registerForm.majorId"
                  class="campus-select"
                  popper-class="campus-select-popper"
                  :placeholder="registerForm.deptId ? '请选择专业' : '请先选择院系'"
                  :disabled="!registerForm.deptId"
                  :teleported="true"
                  @focus="fieldFocus.majorId = true; onTyping()"
                  @blur="fieldFocus.majorId = false"
                  @change="onTyping"
                >
                  <el-option
                    v-for="item in majorOptions"
                    :key="String(item.id)"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </div>
            </el-form-item>

            <el-form-item label="年级" prop="gradeId">
              <div class="input-wrap input-wrap--select" :class="{ focused: fieldFocus.gradeId }">
                                <el-select
                  v-model="registerForm.gradeId"
                  class="campus-select"
                  popper-class="campus-select-popper"
                  placeholder="请选择年级"
                  :teleported="true"
                  @focus="fieldFocus.gradeId = true; onTyping()"
                  @blur="fieldFocus.gradeId = false"
                  @change="onTyping"
                >
                  <el-option
                    v-for="item in gradeOptions"
                    :key="String(item.id)"
                    :label="item.name"
                    :value="item.id"
                  />
                </el-select>
              </div>
            </el-form-item>
          </div>

          <div class="field-grid">
            <el-form-item label="手机号" prop="phone">
              <div class="input-wrap" :class="{ focused: fieldFocus.phone }">
                <input
                  v-model.trim="registerForm.phone"
                  type="text"
                  placeholder="选填"
                  @focus="fieldFocus.phone = true; onTyping()"
                  @blur="fieldFocus.phone = false"
                  @input="onTyping"
                />
              </div>
            </el-form-item>

            <el-form-item label="邮箱" prop="email">
              <div class="input-wrap" :class="{ focused: fieldFocus.email }">
                <input
                  v-model.trim="registerForm.email"
                  type="email"
                  placeholder="选填"
                  @focus="fieldFocus.email = true; onTyping()"
                  @blur="fieldFocus.email = false"
                  @input="onTyping"
                />
              </div>
            </el-form-item>
          </div>

          <button type="submit" class="login-button register-button" :disabled="registerSubmitting" @click="handleRegister">
            {{ registerSubmitting ? '提交中...' : '提交注册申请' }}
          </button>
        </el-form>

        <p class="signup-tip">
          <span v-if="isLoginMode">还没有账号？</span>
          <span v-else>已经注册过？</span>
          <span class="signup-link" @click="toggleMode">{{ isLoginMode ? '立即注册' : '返回登录' }}</span>
        </p>
        <p class="signup-tip secondary">注册信息提交后将进入待审核状态，请留意审核结果通知。</p>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { Hide, View } from '@element-plus/icons-vue';
import AnimatedCharacters from '@/components/auth/AnimatedCharacters.vue';
import { loginApi } from '@/api/modules/auth';
import { queryUserDepartmentsApi, queryUserGradesApi, queryUserMajorsApi, registerApi } from '@/api/modules/users';
import type { IdNameOption, IdValue } from '@/types/api';
import { useAuthStore } from '@/store/modules/auth';

type AuthMode = 'login' | 'register';

interface LoginFormState {
  account: string;
  password: string;
  rememberMe: boolean;
}

interface RegisterFormState {
  studentNo: string;
  realName: string;
  password: string;
  confirmPassword: string;
  deptId: IdValue | null;
  majorId: IdValue | null;
  gradeId: IdValue | null;
  phone: string;
  email: string;
}

const router = useRouter();
const authStore = useAuthStore();

const loginFormRef = ref<FormInstance>();
const registerFormRef = ref<FormInstance>();

const loginSubmitting = ref(false);
const registerSubmitting = ref(false);
const loginPasswordVisible = ref(false);
const registerPasswordVisible = ref(false);
const registerConfirmPasswordVisible = ref(false);
const deptLoading = ref(false);
const majorLoading = ref(false);
const gradeLoading = ref(false);
const activeMode = ref<AuthMode>('login');
const typingFlag = ref(false);
const fieldFocus = reactive({
  account: false,
  password: false,
  studentNo: false,
  realName: false,
  confirmPassword: false,
  deptId: false,
  majorId: false,
  gradeId: false,
  phone: false,
  email: false
});

let typingTimer: number | undefined;

const loginForm = reactive<LoginFormState>({
  account: '',
  password: '',
  rememberMe: true
});

const registerForm = reactive<RegisterFormState>({
  studentNo: '',
  realName: '',
  password: '',
  confirmPassword: '',
  deptId: null,
  majorId: null,
  gradeId: null,
  phone: '',
  email: ''
});

const departmentOptions = ref<IdNameOption[]>([]);
const majorOptions = ref<IdNameOption[]>([]);
const gradeOptions = ref<IdNameOption[]>([]);

const isLoginMode = computed(() => activeMode.value === 'login');
const isTyping = computed(() => typingFlag.value);
const currentPasswordVisible = computed(() =>
  isLoginMode.value ? loginPasswordVisible.value : registerPasswordVisible.value || registerConfirmPasswordVisible.value
);
const currentPasswordLength = computed(() =>
  isLoginMode.value
    ? loginForm.password.length
    : Math.max(registerForm.password.length, registerForm.confirmPassword.length)
);

const loginRules: FormRules<LoginFormState> = {
  account: [{ required: true, message: '请输入学号/工号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
};

const registerRules: FormRules<RegisterFormState> = {
  studentNo: [{ required: true, message: '请输入学号/工号', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'));
          return;
        }
        callback();
      },
      trigger: 'blur'
    }
  ],
  deptId: [{ required: true, message: '请选择院系', trigger: 'change' }],
  majorId: [{ required: true, message: '请选择专业', trigger: 'change' }],
  gradeId: [{ required: true, message: '请选择年级', trigger: 'change' }]
};

function toggleMode() {
  activeMode.value = isLoginMode.value ? 'register' : 'login';
}

function onTyping() {
  typingFlag.value = true;
  if (typingTimer) {
    window.clearTimeout(typingTimer);
  }
  typingTimer = window.setTimeout(() => {
    typingFlag.value = false;
  }, 600);
}

async function loadDepartments() {
  deptLoading.value = true;
  try {
    const resp = await queryUserDepartmentsApi();
    departmentOptions.value = resp.data || [];
  } catch (error) {
    console.error('获取院系列表失败:', error);
  } finally {
    deptLoading.value = false;
  }
}

async function loadGrades() {
  gradeLoading.value = true;
  try {
    const resp = await queryUserGradesApi();
    gradeOptions.value = resp.data || [];
  } catch (error) {
    console.error('获取年级列表失败:', error);
  } finally {
    gradeLoading.value = false;
  }
}

async function loadMajors(deptId: IdValue | null) {
  majorOptions.value = [];
  registerForm.majorId = null;
  if (!deptId) {
    return;
  }
  majorLoading.value = true;
  try {
    const resp = await queryUserMajorsApi(deptId);
    majorOptions.value = resp.data || [];
  } catch (error) {
    console.error('获取专业列表失败:', error);
  } finally {
    majorLoading.value = false;
  }
}

watch(
  () => registerForm.deptId,
  (deptId) => {
    void loadMajors(deptId);
  }
);

async function handleLogin() {
  if (!loginFormRef.value) {
    return;
  }
  await loginFormRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    loginSubmitting.value = true;
    try {
      const resp = await loginApi({
        account: loginForm.account.trim(),
        password: loginForm.password,
        rememberMe: loginForm.rememberMe
      });
      authStore.applyAuthContext({
        context: resp.data,
        rememberMe: loginForm.rememberMe
      });
      if (resp.data.user?.mustChangePassword === 1) {
        ElMessage.warning('检测到密码已被重置，请先修改密码');
      }
      ElMessage.success('登录成功');
      await router.replace('/dashboard');
    } catch (error) {
      console.error('登录失败:', error);
    } finally {
      loginSubmitting.value = false;
    }
  });
}

async function handleRegister() {
  if (!registerFormRef.value) {
    return;
  }
  await registerFormRef.value.validate(async (valid) => {
    if (!valid) {
      return;
    }
    registerSubmitting.value = true;
    try {
      await registerApi({
        studentNo: registerForm.studentNo.trim(),
        realName: registerForm.realName.trim(),
        password: registerForm.password,
        confirmPassword: registerForm.confirmPassword,
        deptId: registerForm.deptId as IdValue,
        majorId: registerForm.majorId as IdValue,
        gradeId: registerForm.gradeId as IdValue,
        phone: registerForm.phone.trim() || undefined,
        email: registerForm.email.trim() || undefined
      });
      ElMessage.success('注册申请已提交，请等待管理员审核');
      registerFormRef.value?.resetFields();
      registerForm.deptId = null;
      registerForm.majorId = null;
      registerForm.gradeId = null;
      registerForm.phone = '';
      registerForm.email = '';
      majorOptions.value = [];
      activeMode.value = 'login';
    } catch (error) {
      console.error('注册失败:', error);
    } finally {
      registerSubmitting.value = false;
    }
  });
}

function handleForgotPassword() {
  ElMessage.info('请联系管理员协助重置密码');
}

onMounted(() => {
  void loadDepartments();
  void loadGrades();
});
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1fr 1fr;
  background:
    radial-gradient(circle at top left, rgba(88, 145, 191, 0.22), transparent 30%),
    radial-gradient(circle at 78% 18%, rgba(176, 196, 163, 0.18), transparent 26%),
    linear-gradient(135deg, #eef4fb 0%, #f7f6f0 52%, #eef7f3 100%);
}

.visual-panel {
  position: relative;
  min-height: 100vh;
  overflow: hidden;
  background:
    radial-gradient(circle at 20% 22%, rgba(255, 248, 214, 0.22), transparent 24%),
    radial-gradient(circle at 76% 24%, rgba(186, 224, 204, 0.22), transparent 26%),
    radial-gradient(circle at 62% 58%, rgba(155, 186, 230, 0.28), transparent 34%),
    linear-gradient(160deg, #6f89a7 0%, #879cb4 38%, #6fa296 72%, #d8c9a1 100%);
}

.visual-panel::before,
.visual-panel::after {
  content: '';
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
  filter: blur(2px);
}

.visual-panel::before {
  inset: 10% auto auto 60%;
  width: 240px;
  height: 240px;
  background: rgba(255, 255, 255, 0.08);
}

.visual-panel::after {
  inset: auto auto 8% 12%;
  width: 180px;
  height: 180px;
  background: rgba(255, 244, 203, 0.08);
}

.visual-content {
  min-height: 100vh;
  padding: 48px 48px 42px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  gap: 24px;
}

.brand-block {
  color: #fff;
}

.brand-pill {
  display: inline-flex;
  align-items: center;
  padding: 8px 14px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(12px);
  font-size: 13px;
  letter-spacing: 0.04em;
}

.brand-block h1 {
  margin: 18px 0 0;
  font-size: clamp(30px, 2.8vw, 44px);
  line-height: 1.15;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.visual-stage {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  position: relative;
}

.visual-stage::before {
  content: '';
  position: absolute;
  inset: auto 20% 12% 20%;
  height: 10px;
  background: radial-gradient(circle, rgba(220, 229, 246, 0.26), rgba(220, 229, 246, 0));
  pointer-events: none;
}

.visual-stage :deep(.characters-root) {
  margin-left: -40px;
  margin-top: 20px;
}

.form-panel {
  min-height: 100vh;
  background:
    radial-gradient(circle at 16% 16%, rgba(124, 156, 214, 0.14), transparent 24%),
    radial-gradient(circle at 84% 76%, rgba(196, 180, 131, 0.12), transparent 22%),
    linear-gradient(180deg, rgba(250, 252, 255, 0.96) 0%, rgba(245, 247, 242, 0.98) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
}

.login-card {
  width: min(560px, 100%);
  padding: 28px 28px 24px;
  border-radius: 28px;
  border: 1px solid rgba(148, 163, 184, 0.18);
  background: rgba(255, 255, 255, 0.82);
  box-shadow: 0 28px 90px rgba(15, 23, 42, 0.08);
  backdrop-filter: blur(14px);
}

.login-head {
  text-align: center;
  margin-bottom: 30px;
}

.login-head h1 {
  margin: 0;
  font-size: clamp(34px, 2.2vw, 40px);
  line-height: 1.1;
  color: #030712;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.login-head p {
  margin: 12px auto 0;
  max-width: 34rem;
  color: #64748b;
  font-size: 14px;
  line-height: 1.8;
}

.login-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.login-form :deep(.el-form-item),
.login-form :deep(.el-form-item__content) {
  width: 100%;
}

.field-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.field-grid--triple {
  grid-template-columns: 1.1fr 1fr 0.8fr;
}

.input-wrap {
  width: 100%;
  min-height: 56px;
  border-radius: 18px;
  border: 1px solid rgba(145, 164, 198, 0.34);
  background: rgba(248, 250, 252, 0.88);
  display: flex;
  align-items: center;
  padding: 0 18px;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, background-color 0.2s ease, transform 0.2s ease;
}

.input-wrap.focused {
  border-color: rgba(79, 91, 216, 0.55);
  box-shadow: 0 0 0 2px rgba(106, 141, 196, 0.14);
  background: rgba(255, 255, 255, 0.96);
  transform: translateY(-1px);
}

.input-wrap input {
  flex: 1;
  border: 0;
  outline: 0;
  background: transparent;
  color: #0f172a;
  font-size: 16px;
}

.input-wrap input::placeholder {
  color: #9ba3b3;
}

.input-wrap input::-ms-reveal,
.input-wrap input::-ms-clear {
  display: none;
}

.input-wrap--select {
  padding-right: 18px;
}

.campus-select {
  width: 100%;
  flex: 1;
  min-width: 0;
  display: block;
  align-self: stretch;
}

.input-wrap--select :deep(.el-select) {
  width: 100%;
  flex: 1;
  min-width: 0;
  display: flex;
  align-self: stretch;
}

.input-wrap--select :deep(.el-select__wrapper) {
  width: 100%;
  flex: 1;
  min-width: 0;
  align-self: stretch;
  min-height: 56px;
  padding: 0;
  border: 0;
  border-radius: 16px;
  background: transparent;
  box-shadow: none;
}

.input-wrap--select :deep(.el-select__placeholder),
.input-wrap--select :deep(.el-select__selected-item),
.input-wrap--select :deep(.el-select__input) {
  color: #0f172a;
  font-size: 16px;
  justify-content: flex-start;
}

.input-wrap--select :deep(.el-select__caret) {
  color: #7c8ba5;
}

.input-wrap--select :deep(.is-disabled) {
  cursor: not-allowed;
}

:global(.campus-select-popper) {
  padding: 8px;
  border: 1px solid rgba(163, 182, 207, 0.22);
  border-radius: 18px;
  background:
    radial-gradient(circle at top right, rgba(255, 248, 214, 0.18), transparent 28%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(246, 250, 255, 0.96) 100%);
  box-shadow: 0 20px 42px rgba(31, 41, 55, 0.14);
  backdrop-filter: blur(14px);
}

:global(.campus-select-popper .el-select-dropdown__wrap) {
  max-height: 280px;
}

:global(.campus-select-popper .el-select-dropdown__item) {
  height: 42px;
  margin: 2px 0;
  padding: 0 14px;
  border-radius: 12px;
  color: #1f2937;
  line-height: 42px;
}

:global(.campus-select-popper .el-select-dropdown__item.hover),
:global(.campus-select-popper .el-select-dropdown__item:hover) {
  background: rgba(96, 141, 190, 0.1);
  color: #214e78;
}

:global(.campus-select-popper .el-select-dropdown__item.selected) {
  background: rgba(96, 141, 190, 0.16);
  color: #214e78;
  font-weight: 600;
}

:global(.campus-select-popper .el-select-dropdown__empty) {
  padding: 12px 14px;
  color: #7c8ba5;
}

.password-toggle,
.plain-button {
  border: 0;
  background: transparent;
  padding: 0;
  font: inherit;
}

.password-toggle {
  color: #6b7280;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.password-toggle:hover {
  color: #4f5bd8;
}

.login-meta {
  margin-top: 2px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.remember-me {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  color: #334155;
  font-size: 14px;
  user-select: none;
  cursor: pointer;
}

.remember-me input {
  width: 16px;
  height: 16px;
  accent-color: #4f5bd8;
}

.forgot-link {
  border: 0;
  background: transparent;
  padding: 0;
  font: inherit;
  color: #4f5bd8;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.login-button {
  width: 100%;
  min-height: 56px;
  border: 0;
  border-radius: 18px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
  margin-top: 2px;
  color: #ffffff;
  background: linear-gradient(135deg, #4f5bd8, #5f48e1);
  box-shadow: 0 14px 28px rgba(79, 91, 216, 0.22);
}

.login-button:hover {
  transform: translateY(-1px);
  box-shadow: 0 14px 30px rgba(79, 91, 216, 0.32);
}

.login-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}

.register-button {
  margin-top: 8px;
}

.signup-tip {
  margin: 26px 0 0;
  text-align: center;
  color: #64748b;
  font-size: 15px;
}

.signup-tip.secondary {
  margin-top: 8px;
  line-height: 1.7;
}

.signup-link {
  margin-left: 6px;
  color: #4f5bd8;
  font-weight: 600;
  cursor: pointer;
}

.signup-link:hover {
  text-decoration: underline;
}

@media (max-width: 1100px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .visual-panel,
  .form-panel,
  .visual-content {
    min-height: auto;
  }

  .visual-panel {
    border-right: 0;
    border-bottom: 1px solid rgba(15, 23, 42, 0.1);
  }

  .visual-content {
    padding: 28px 22px 24px;
  }

  .visual-stage {
    min-height: 320px;
  }

  .visual-stage::before {
    bottom: 68px;
    transform: translateX(-50%);
  }

  .visual-stage :deep(.characters-root) {
    margin-left: -72px;
    margin-top: 0;
  }

  .form-panel {
    padding: 28px 18px 36px;
  }

  .login-card {
    max-width: 560px;
  }
}

@media (max-width: 860px) {
  .login-card {
    width: min(100%, 560px);
    padding: 24px 20px 20px;
  }

  .field-grid--triple {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .field-grid {
    grid-template-columns: 1fr;
  }

  .login-card {
    padding: 20px 16px 16px;
    border-radius: 24px;
  }

  .visual-content {
    padding: 20px 16px 18px;
  }

  .login-head h1 {
    font-size: 30px;
  }

  .input-wrap,
  .login-button {
    min-height: 50px;
  }
}
</style>


