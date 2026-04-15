<template>
  <div class="sidebar-wrap">
    <div class="sidebar-body">
      <div class="sidebar-section">
        <div class="sidebar-section__label">WELCOME</div>
        <el-scrollbar class="sidebar-scroll">
          <el-menu
            :default-active="activePath"
            class="sidebar-menu"
            router
          >
            <sidebar-menu-item
              v-for="menu in menus"
              :key="menu.id"
              :item="menu"
            />
          </el-menu>
        </el-scrollbar>
      </div>
    </div>

    <div class="sidebar-user">
      <div class="sidebar-user__main">
        <div class="sidebar-user__avatar">
          <el-icon><User /></el-icon>
        </div>
        <div class="sidebar-user__info">
          <div class="sidebar-user__name">{{ displayName }}</div>
          <div class="sidebar-user__email">{{ displayEmail }}</div>
        </div>
      </div>

      <el-dropdown
        v-if="canEditProfile"
        trigger="click"
        placement="top-end"
        @command="handleAccountCommand"
      >
        <button class="sidebar-user__more" type="button" aria-label="Account Menu">
          <el-icon><Expand /></el-icon>
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">个人资料</el-dropdown-item>
            <el-dropdown-item command="password">修改密码</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>

  <el-dialog
    v-model="forcePasswordDialogVisible"
    title="首次登录修改密码"
    width="560px"
    destroy-on-close
    :show-close="false"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <el-alert
      title="检测到你的密码是管理员重置后的初始密码，请先修改密码后再继续使用系统。"
      type="warning"
      :closable="false"
      show-icon
      class="password-force-tip"
    />

    <el-form ref="forcePasswordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
      <el-form-item label="当前密码" prop="oldPassword">
        <el-input
          v-model="passwordForm.oldPassword"
          type="password"
          show-password
          placeholder="请输入管理员重置后的密码"
        />
      </el-form-item>
      <el-form-item label="新密码" prop="newPassword">
        <el-input
          v-model="passwordForm.newPassword"
          type="password"
          show-password
          placeholder="请输入新密码"
        />
      </el-form-item>
      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
          v-model="passwordForm.confirmPassword"
          type="password"
          show-password
          placeholder="请再次输入新密码"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button type="primary" :loading="passwordSubmitting" @click="handleForcePasswordUpdate">
        立即修改
      </el-button>
    </template>
  </el-dialog>

  <el-dialog v-model="profileDialogVisible" title="个人资料" width="560px" destroy-on-close class="profile-dialog">
    <el-tabs v-model="profileActiveTab" class="profile-tabs">
      <el-tab-pane label="基本资料" name="profile">
        <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px" class="profile-form">
          <el-form-item label="姓名" prop="realName">
            <el-input v-model.trim="profileForm.realName" maxlength="32" placeholder="请输入姓名" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model.trim="profileForm.phone" maxlength="20" placeholder="请输入手机号" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model.trim="profileForm.email" maxlength="100" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item v-if="authStore.userInfo?.deptName" label="院系">
            <el-input :model-value="authStore.userInfo?.deptName" disabled />
          </el-form-item>
          <el-form-item v-if="authStore.userInfo?.majorName" label="专业">
            <el-input :model-value="authStore.userInfo?.majorName" disabled />
          </el-form-item>
          <el-form-item v-if="authStore.userInfo?.gradeName" label="年级">
            <el-input :model-value="authStore.userInfo?.gradeName" disabled />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="修改密码" name="password">
        <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" class="profile-form">
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              show-password
              placeholder="请输入当前密码"
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              show-password
              placeholder="请输入新密码"
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              show-password
              placeholder="请再次输入新密码"
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>

    <template #footer>
      <el-button @click="profileDialogVisible = false">取消</el-button>
      <el-button
        v-if="profileActiveTab === 'profile'"
        type="primary"
        :loading="profileSubmitting"
        @click="handleUpdateProfile"
      >
        保存资料
      </el-button>
      <el-button
        v-else
        type="primary"
        :loading="passwordSubmitting"
        @click="handleUpdatePassword"
      >
        更新密码
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { Expand, User } from '@element-plus/icons-vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useRoute } from 'vue-router';
import { updatePasswordApi, updateProfileApi } from '@/api/modules/users';
import { useAuthStore } from '@/store/modules/auth';
import SidebarMenuItem from './SidebarMenuItem.vue';

const route = useRoute();
const authStore = useAuthStore();

function flattenMenuPaths(items: typeof authStore.menuTree): string[] {
  const paths: string[] = [];
  items.forEach((item) => {
    if (item.routePath) {
      paths.push(item.routePath);
    }
    if (item.children?.length) {
      paths.push(...flattenMenuPaths(item.children));
    }
  });
  return paths;
}

const activePath = computed(() => {
  const currentPath = route.path || '/dashboard';
  const menuPaths = flattenMenuPaths(authStore.menuTree || []).filter((item) => !!item);
  if (menuPaths.includes(currentPath)) {
    return currentPath;
  }
  const matchedPath = menuPaths
    .filter((item) => currentPath.startsWith(item))
    .sort((left, right) => right.length - left.length)[0];
  return matchedPath || currentPath;
});

const menus = computed(() => authStore.menuTree || []);
const displayName = computed(() => authStore.userInfo?.realName || authStore.userInfo?.accountNo || '用户');
const displayEmail = computed(() => authStore.userInfo?.email || '暂无邮箱');
const canEditProfile = computed(() => authStore.permissionCodes.includes('auth:profile'));
const profileDialogVisible = ref(false);
const forcePasswordDialogVisible = ref(false);
const profileActiveTab = ref<'profile' | 'password'>('profile');
const profileSubmitting = ref(false);
const passwordSubmitting = ref(false);
const profileFormRef = ref<FormInstance>();
const passwordFormRef = ref<FormInstance>();
const forcePasswordFormRef = ref<FormInstance>();
const profileForm = reactive({
  realName: '',
  phone: '',
  email: ''
});
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const profileRules: FormRules = {
  realName: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }]
};

const passwordRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请再次输入新密码', trigger: 'blur' }]
};

const forcePasswordChange = computed(() => authStore.userInfo?.mustChangePassword === 1);
const profileDialogTitle = computed(() => (forcePasswordChange.value ? '首次登录修改密码' : '个人资料'));

function resetPasswordForm() {
  passwordForm.oldPassword = '';
  passwordForm.newPassword = '';
  passwordForm.confirmPassword = '';
  passwordFormRef.value?.clearValidate();
  forcePasswordFormRef.value?.clearValidate();
}

function openProfileDialog(tab: 'profile' | 'password' = 'profile') {
  if (forcePasswordChange.value) {
    forcePasswordDialogVisible.value = true;
    resetPasswordForm();
    return;
  }
  profileDialogVisible.value = true;
  profileActiveTab.value = tab;
  profileForm.realName = authStore.userInfo?.realName || '';
  profileForm.phone = authStore.userInfo?.phone || '';
  profileForm.email = authStore.userInfo?.email || '';
  profileFormRef.value?.clearValidate();
  resetPasswordForm();
}

function handleForcePasswordDialogClosed() {
  if (!forcePasswordChange.value) {
    forcePasswordDialogVisible.value = false;
  }
}

function handleAccountCommand(command: string) {
  if (command === 'profile') {
    openProfileDialog('profile');
    return;
  }
  if (command === 'password') {
    openProfileDialog('password');
  }
}

async function handleUpdateProfile() {
  const valid = await profileFormRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }
  profileSubmitting.value = true;
  try {
    await updateProfileApi({
      realName: profileForm.realName.trim(),
      phone: profileForm.phone.trim() || undefined,
      email: profileForm.email.trim() || undefined
    });
    await authStore.refreshMe();
    ElMessage.success('个人资料已更新');
    profileDialogVisible.value = false;
  } finally {
    profileSubmitting.value = false;
  }
}

async function handleUpdatePassword(formRef: typeof passwordFormRef = passwordFormRef) {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致');
    return;
  }
  passwordSubmitting.value = true;
  try {
    await updatePasswordApi({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword,
      confirmPassword: passwordForm.confirmPassword
    });
    await authStore.refreshMe();
    ElMessage.success('密码修改成功');
    profileDialogVisible.value = false;
    forcePasswordDialogVisible.value = false;
    profileActiveTab.value = 'profile';
    resetPasswordForm();
  } finally {
    passwordSubmitting.value = false;
  }
}

async function handleForcePasswordUpdate() {
  await handleUpdatePassword(forcePasswordFormRef);
}

watch(
  () => authStore.userInfo?.mustChangePassword,
  (mustChangePassword) => {
    if (mustChangePassword === 1) {
      profileDialogVisible.value = false;
      forcePasswordDialogVisible.value = true;
      resetPasswordForm();
      return;
    }
    forcePasswordDialogVisible.value = false;
  },
  { immediate: true }
);
</script>

<style scoped lang="scss">
.profile-dialog {
  :deep(.el-dialog) {
    border-radius: 18px;
    overflow: hidden;
    box-shadow: 0 24px 80px rgba(15, 23, 42, 0.18);
  }

  :deep(.el-dialog__header) {
    margin-right: 0;
    padding: 22px 24px 10px;
  }

  :deep(.el-dialog__title) {
    font-size: 18px;
    font-weight: 600;
    color: #0f172a;
    letter-spacing: 0.2px;
  }

  :deep(.el-dialog__headerbtn) {
    top: 18px;
    right: 18px;
    width: 32px;
    height: 32px;
    border-radius: 999px;
    transition: background-color 0.2s ease, color 0.2s ease;
  }

  :deep(.el-dialog__headerbtn:hover) {
    background: #f1f5f9;
  }

  :deep(.el-dialog__body) {
    padding: 0 24px 18px;
    background: linear-gradient(180deg, #f8fbff 0%, #ffffff 42%);
  }

  :deep(.el-dialog__footer) {
    padding: 14px 24px 24px;
    background: #ffffff;
  }
}

.profile-tabs {
  :deep(.el-tabs__header) {
    margin-bottom: 18px;
  }

  :deep(.el-tabs__nav-wrap::after) {
    height: 1px;
    background: linear-gradient(90deg, rgba(148, 163, 184, 0.18), rgba(148, 163, 184, 0.5), rgba(148, 163, 184, 0.18));
  }

  :deep(.el-tabs__item) {
    height: 44px;
    padding: 0 2px;
    font-size: 14px;
    color: #64748b;
    transition: color 0.2s ease;
  }

  :deep(.el-tabs__item:hover) {
    color: #3b82f6;
  }

  :deep(.el-tabs__item.is-active) {
    color: #2563eb;
    font-weight: 600;
  }

  :deep(.el-tabs__active-bar) {
    height: 3px;
    border-radius: 999px;
    background: linear-gradient(90deg, #3b82f6, #60a5fa);
  }
}

.profile-form {
  padding: 8px 4px 2px;

  :deep(.el-form-item) {
    margin-bottom: 20px;
  }

  :deep(.el-form-item__label) {
    color: #475569;
    font-weight: 500;
  }

  :deep(.el-input__wrapper) {
    min-height: 40px;
    border-radius: 10px;
    box-shadow: inset 0 0 0 1px #d7dee8;
    background: #ffffff;
    transition: box-shadow 0.2s ease, transform 0.2s ease;
  }

  :deep(.el-input__wrapper:hover) {
    box-shadow: inset 0 0 0 1px #93c5fd;
  }

  :deep(.el-input__wrapper.is-focus) {
    box-shadow: inset 0 0 0 1px #3b82f6, 0 0 0 3px rgba(59, 130, 246, 0.12);
  }

  :deep(.el-input.is-disabled .el-input__wrapper) {
    background: #f8fafc;
    box-shadow: inset 0 0 0 1px #e2e8f0;
  }

  :deep(.el-input.is-disabled .el-input__inner) {
    color: #94a3b8;
  }
}

.profile-dialog {
  :deep(.el-button) {
    min-width: 78px;
    height: 36px;
    border-radius: 10px;
    font-weight: 500;
  }

  :deep(.el-button--default) {
    border-color: #cbd5e1;
    color: #475569;
  }

  :deep(.el-button--default:hover) {
    border-color: #94a3b8;
    color: #334155;
    background: #f8fafc;
  }

  :deep(.el-button--primary) {
    box-shadow: 0 10px 18px rgba(59, 130, 246, 0.22);
  }
}

.sidebar-wrap {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  color: #111827;
}

.sidebar-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  padding: 22px 16px 16px;
}

.sidebar-section {
  flex: 1;
  min-height: 0;
}

.sidebar-section__label {
  margin-bottom: 14px;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  text-align: center;
}

.sidebar-scroll {
  height: 100%;
}

.sidebar-menu {
  border-right: none;
  background: transparent;
}

.sidebar-user {
  padding: 16px;
  border-top: 1px solid #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.sidebar-user__main {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.sidebar-user__avatar {
  width: 32px;
  height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #111827;
  flex-shrink: 0;
}

.sidebar-user__info {
  min-width: 0;
}

.sidebar-user__name {
  font-size: 14px;
  font-weight: 500;
  color: #111827;
  line-height: 1.2;
}

.sidebar-user__email {
  margin-top: 2px;
  font-size: 12px;
  color: #6b7280;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar-user__more {
  width: 28px;
  height: 28px;
  border: none;
  background: transparent;
  color: #111827;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  cursor: pointer;
}

:deep(.sidebar-menu.el-menu) {
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #111827;
  --el-menu-hover-bg-color: #f5f5f5;
  --el-menu-active-color: #111827;
  border-right: none;
}

:deep(.sidebar-menu .el-menu-item),
:deep(.sidebar-menu .el-sub-menu__title) {
  height: 44px;
  margin-bottom: 8px;
  padding-left: 0 !important;
  padding-right: 12px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 400;
  color: #111827;
}

:deep(.sidebar-menu .el-menu-item.is-active),
:deep(.sidebar-menu .el-sub-menu__title:hover),
:deep(.sidebar-menu .el-menu-item:hover) {
  background: #f5f5f5;
  color: #111827;
}

:deep(.sidebar-menu .el-sub-menu .el-menu) {
  background: transparent;
}

:deep(.sidebar-scroll .el-scrollbar__view) {
  padding-bottom: 4px;
}
</style>
