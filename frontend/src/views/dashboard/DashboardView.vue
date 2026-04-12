<template>
  <div class="dashboard-page">
    <section class="welcome-panel">
      <div class="welcome-panel__main">
        <div class="welcome-panel__tag">工作台</div>
        <h1 class="welcome-panel__title">{{ greeting }}，{{ displayName }}</h1>
      </div>
      <div class="welcome-panel__meta">
        <div class="meta-label">当前角色</div>
        <div class="meta-value">{{ currentRoleName }}</div>
      </div>
    </section>

    <section class="overview-grid">
      <div class="overview-card">
        <div class="overview-card__label">未读消息</div>
        <div class="overview-card__value">{{ unreadDisplay }}</div>
        <button v-if="canViewNotice" class="overview-card__link" type="button" @click="goNoticeCenter">
          进入消息中心
        </button>
        <div v-else class="overview-card__hint">当前角色暂无消息中心权限</div>
      </div>

      <div class="overview-card">
        <div class="overview-card__label">快捷入口</div>
        <div class="overview-card__value">{{ quickLinks.length }}</div>
      </div>

      <div class="overview-card">
        <div class="overview-card__label">系统状态</div>
        <div class="overview-card__value">正常</div>
        <div class="overview-card__hint">页面和基础数据加载正常</div>
      </div>
    </section>

    <section class="content-grid">
      <div class="panel">
        <div class="panel__head">
          <div>
            <div class="panel__title">常用功能</div>
          </div>
        </div>

        <div v-if="quickLinks.length" class="quick-links">
          <button
            v-for="item in quickLinks"
            :key="item.routePath"
            class="quick-link"
            type="button"
            @click="goRoute(item.routePath)"
          >
            <span class="quick-link__name">{{ item.menuName }}</span>
            <span class="quick-link__path">{{ item.routePath }}</span>
          </button>
        </div>

        <el-empty v-else description="当前角色暂无可访问的业务模块" />
      </div>

      <div class="panel">
        <div class="panel__head">
          <div>
            <div class="panel__title">消息提醒</div>
          </div>
          <el-button v-if="canViewNotice" link type="primary" @click="goNoticeCenter">
            全部消息
          </el-button>
        </div>

        <el-empty
          v-if="!canViewNotice"
          description="当前角色没有消息中心访问权限"
        />

        <el-skeleton v-else-if="loading" :rows="5" animated />

        <div v-else-if="recentNotices.length" class="notice-list">
          <button
            v-for="item in recentNotices"
            :key="item.noticeId"
            class="notice-item"
            type="button"
            @click="goNoticeDetail(item.noticeId)"
          >
            <div class="notice-item__top">
              <span class="notice-item__badge" :class="{ 'is-urgent': item.noticeLevel === 'URGENT' }">
                {{ item.noticeLevel === 'URGENT' ? '紧急' : '通知' }}
              </span>
              <span class="notice-item__time">{{ item.sendTime || '-' }}</span>
            </div>
            <div class="notice-item__title">{{ item.noticeTitle }}</div>
          </button>
        </div>

        <el-empty v-else description="暂无未读消息" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import { queryRecentNoticesApi } from '@/api/modules/notice';
import { useAuthStore } from '@/store/modules/auth';
import { useNoticeStore } from '@/store/modules/notice';
import type { IdValue, MenuNode, NoticeListItem } from '@/types/api';

const router = useRouter();
const authStore = useAuthStore();
const noticeStore = useNoticeStore();

const canViewNotice = computed(() => authStore.permissionCodes.includes('notice:view'));
const loading = ref(false);
const recentNotices = ref<NoticeListItem[]>([]);

const displayName = computed(() => authStore.userInfo?.realName || authStore.userInfo?.accountNo || '用户');

const currentRoleName = computed(() => {
  const currentRole = authStore.roleOptions.find((item) => item.roleCode === authStore.currentRoleCode);
  return currentRole?.roleName || authStore.currentRoleCode || '-';
});

const unreadDisplay = computed(() => {
  if (!canViewNotice.value) {
    return '--';
  }
  return noticeStore.unreadCount > 99 ? '99+' : String(noticeStore.unreadCount);
});

const greeting = computed(() => {
  const hour = new Date().getHours();
  if (hour < 12) {
    return '上午好';
  }
  if (hour < 18) {
    return '下午好';
  }
  return '晚上好';
});

function collectQuickLinks(items: MenuNode[], bucket: MenuNode[] = []) {
  items.forEach((item) => {
    if (item.routePath && item.routePath !== '/dashboard') {
      bucket.push(item);
    }
    if (item.children?.length) {
      collectQuickLinks(item.children, bucket);
    }
  });
  return bucket;
}

const quickLinks = computed(() => {
  const items = collectQuickLinks(authStore.menuTree || []);
  const unique = new Map<string, MenuNode>();

  items.forEach((item) => {
    if (!unique.has(item.routePath)) {
      unique.set(item.routePath, item);
    }
  });

  return Array.from(unique.values()).slice(0, 6);
});

async function fetchRecentUnread() {
  if (!canViewNotice.value) {
    recentNotices.value = [];
    return;
  }
  loading.value = true;
  try {
    const resp = await queryRecentNoticesApi({
      limit: 5,
      unreadOnly: 1
    });
    recentNotices.value = resp.data || [];
  } finally {
    loading.value = false;
  }
}

function goNoticeCenter() {
  router.push('/notice');
}

function goNoticeDetail(noticeId: IdValue) {
  router.push(`/notice/detail/${noticeId}`);
}

function goRoute(routePath: string) {
  router.push(routePath);
}

watch(
  () => noticeStore.unreadCount,
  () => {
    fetchRecentUnread();
  }
);

onMounted(() => {
  fetchRecentUnread();
});
</script>

<style scoped lang="scss">
.dashboard-page {
  display: flex;
  flex-direction: column;
  gap: 20px;
  background:
    radial-gradient(circle at top left, rgba(88, 145, 191, 0.1), transparent 28%),
    radial-gradient(circle at 92% 12%, rgba(176, 196, 163, 0.08), transparent 26%),
    linear-gradient(180deg, rgba(246, 249, 253, 0.68) 0%, rgba(250, 248, 241, 0.34) 100%);
  border-radius: 24px;
  padding: 12px;
}

.welcome-panel {
  display: flex;
  align-items: stretch;
  justify-content: space-between;
  gap: 20px;
  padding: 28px 32px;
  border-radius: 20px;
  background:
    radial-gradient(circle at 18% 20%, rgba(255, 247, 223, 0.28), transparent 24%),
    radial-gradient(circle at 78% 18%, rgba(192, 218, 203, 0.2), transparent 26%),
    linear-gradient(135deg, rgba(255, 255, 255, 0.94) 0%, rgba(242, 247, 252, 0.92) 100%);
  border: 1px solid rgba(163, 182, 207, 0.2);
  box-shadow: 0 16px 34px rgba(15, 23, 42, 0.06);
}

.welcome-panel__main {
  min-width: 0;
}

.welcome-panel__tag {
  display: inline-flex;
  align-items: center;
  height: 28px;
  padding: 0 12px;
  border-radius: 999px;
  background: rgba(77, 120, 174, 0.12);
  color: #315f8f;
  font-size: 12px;
  font-weight: 600;
}

.welcome-panel__title {
  margin: 16px 0 10px;
  font-size: 34px;
  line-height: 1.15;
  color: #0f172a;
}

.welcome-panel__meta {
  width: 280px;
  padding: 22px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.82);
  border: 1px solid rgba(163, 182, 207, 0.18);
  flex-shrink: 0;
}

.meta-label {
  font-size: 13px;
  color: #64748b;
}

.meta-value {
  margin-top: 10px;
  font-size: 26px;
  font-weight: 700;
  color: #111827;
  line-height: 1.2;
}

.meta-tip {
  margin-top: 12px;
  font-size: 13px;
  line-height: 1.7;
  color: #64748b;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.overview-card {
  padding: 22px;
  border-radius: 18px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.94) 0%, rgba(250, 252, 255, 0.88) 100%);
  border: 1px solid rgba(163, 182, 207, 0.16);
  box-shadow: 0 10px 24px rgba(15, 23, 42, 0.04);
}

.overview-card__label {
  font-size: 13px;
  color: #64748b;
}

.overview-card__value {
  margin-top: 10px;
  font-size: 30px;
  line-height: 1.1;
  font-weight: 700;
  color: #0f172a;
}

.overview-card__hint {
  margin-top: 10px;
  font-size: 13px;
  line-height: 1.7;
  color: #64748b;
}

.overview-card__link {
  margin-top: 10px;
  padding: 0;
  border: none;
  background: transparent;
  color: #315f8f;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
}

.content-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.2fr) minmax(340px, 0.8fr);
  gap: 20px;
}

.panel {
  min-width: 0;
  padding: 24px;
  border-radius: 20px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.95) 0%, rgba(250, 252, 255, 0.9) 100%);
  border: 1px solid rgba(163, 182, 207, 0.16);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.05);
}

.panel__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.panel__title {
  font-size: 22px;
  font-weight: 700;
  color: #0f172a;
}

.quick-links {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.quick-link {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 18px;
  border-radius: 16px;
  border: 1px solid rgba(163, 182, 207, 0.14);
  background:
    radial-gradient(circle at top right, rgba(255, 247, 223, 0.3), transparent 32%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(246, 250, 255, 0.94) 100%);
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.quick-link:hover {
  transform: translateY(-2px);
  border-color: rgba(77, 120, 174, 0.26);
  box-shadow: 0 12px 24px rgba(77, 120, 174, 0.08);
}

.quick-link__name {
  font-size: 16px;
  font-weight: 600;
  color: #0f172a;
}

.quick-link__path {
  font-size: 12px;
  color: #94a3b8;
}

.notice-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.notice-item {
  width: 100%;
  padding: 16px;
  border: 1px solid rgba(163, 182, 207, 0.14);
  border-radius: 16px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.96) 0%, rgba(249, 251, 255, 0.94) 100%);
  text-align: left;
  cursor: pointer;
  transition: transform 0.18s ease, box-shadow 0.18s ease, border-color 0.18s ease;
}

.notice-item:hover {
  transform: translateY(-1px);
  border-color: rgba(77, 120, 174, 0.26);
  box-shadow: 0 10px 22px rgba(77, 120, 174, 0.08);
}

.notice-item__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.notice-item__badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 44px;
  height: 24px;
  padding: 0 10px;
  border-radius: 999px;
  background: rgba(77, 120, 174, 0.12);
  color: #315f8f;
  font-size: 12px;
  font-weight: 600;
}

.notice-item__badge.is-urgent {
  background: #fee2e2;
  color: #dc2626;
}

.notice-item__time {
  font-size: 12px;
  color: #94a3b8;
}

.notice-item__title {
  margin-top: 12px;
  color: #1f2937;
  font-size: 15px;
  line-height: 1.6;
  font-weight: 500;
}

@media (max-width: 1200px) {
  .welcome-panel,
  .content-grid {
    flex-direction: column;
    grid-template-columns: 1fr;
  }

  .welcome-panel__meta {
    width: auto;
  }
}

@media (max-width: 768px) {
  .welcome-panel,
  .panel,
  .overview-card {
    padding: 18px;
  }

  .welcome-panel__title {
    font-size: 28px;
  }

  .overview-grid,
  .quick-links {
    grid-template-columns: 1fr;
  }
}
</style>
