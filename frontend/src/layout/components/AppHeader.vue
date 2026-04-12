<template>
  <div class="header-wrap">
    <div class="header-title">{{ appTitle }}</div>
    <div class="header-right">
      <el-select
        v-model="selectedRole"
        class="role-select"
        size="small"
        placeholder="切换角色"
        @change="handleSwitchRole"
      >
        <el-option
          v-for="role in authStore.roleOptions"
          :key="role.roleCode"
          :label="role.roleName"
          :value="role.roleCode"
        />
      </el-select>
      <el-badge
        class="notice-badge"
        :value="unreadBadgeValue"
        :hidden="!canViewNotice || noticeStore.unreadCount <= 0"
      >
        <el-button type="primary" link @click="goNoticeCenter">消息中心</el-button>
      </el-badge>
      <el-button type="danger" link @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { logoutApi, switchRoleApi } from '@/api/modules/auth';
import { useAuthStore } from '@/store/modules/auth';
import { useNoticeStore } from '@/store/modules/notice';

const router = useRouter();
const authStore = useAuthStore();
const noticeStore = useNoticeStore();

const appTitle = '师范生教育实习全过程管理平台';
const selectedRole = ref(authStore.currentRoleCode);
const canViewNotice = computed(() => authStore.permissionCodes.includes('notice:view'));
const unreadBadgeValue = computed(() => (noticeStore.unreadCount > 99 ? '99+' : String(noticeStore.unreadCount)));

watch(
  () => authStore.currentRoleCode,
  (value) => {
    selectedRole.value = value;
  }
);

watch(
  () => [authStore.token, authStore.currentRoleCode, canViewNotice.value],
  ([token, _roleCode, canView]) => {
    if (!token || !canView) {
      noticeStore.stopPolling();
      noticeStore.setUnreadCount(0);
      return;
    }
    noticeStore.refreshUnreadCount().catch(() => undefined);
    noticeStore.startPolling();
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  noticeStore.stopPolling();
});

async function handleSwitchRole(roleCode: string) {
  if (!roleCode || roleCode === authStore.currentRoleCode) {
    return;
  }

  const resp = await switchRoleApi({ roleCode });
  authStore.applyAuthContext({
    context: resp.data,
    rememberMe: authStore.rememberMe
  });
  ElMessage.success('角色切换成功');
  await router.replace('/dashboard');
}

function goNoticeCenter() {
  if (!canViewNotice.value) {
    return;
  }
  router.push('/notice');
}

async function handleLogout() {
  await ElMessageBox.confirm('确认退出当前账号吗？', '提示', {
    confirmButtonText: '确认',
    cancelButtonText: '取消',
    type: 'warning'
  });

  try {
    await logoutApi();
  } finally {
    authStore.resetAuth();
    noticeStore.reset();
  }
  await router.replace('/login');
}
</script>

<style scoped lang="scss">
.header-wrap {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-title {
  font-weight: 600;
  color: #1f2329;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.role-select {
  width: 180px;
}

.notice-badge {
  display: inline-flex;
}
</style>
