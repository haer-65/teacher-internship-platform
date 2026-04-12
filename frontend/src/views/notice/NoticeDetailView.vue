<template>
  <div class="notice-detail-page">
    <el-empty
      v-if="!canViewNotice"
      description="当前角色没有消息中心访问权限"
    />

    <el-card v-else v-loading="loading">
      <template #header>
        <div class="header-row">
          <span class="title">消息详情</span>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </template>

      <el-empty v-if="!detail" description="消息不存在或已被删除" />

      <template v-else>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="标题">{{ detail.noticeTitle }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="detail.readFlag === 1 ? 'success' : 'warning'">
              {{ detail.readFlag === 1 ? '已读' : '未读' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="类型">{{ noticeTypeLabel(detail.noticeType) }}</el-descriptions-item>
          <el-descriptions-item label="级别">
            <el-tag :type="detail.noticeLevel === 'URGENT' ? 'danger' : 'info'">
              {{ noticeLevelLabel(detail.noticeLevel) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发送人">{{ detail.senderName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发送时间">{{ detail.sendTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="关联业务">{{ relatedBusinessTypeLabel(detail.relatedBusinessType) }}</el-descriptions-item>
          <el-descriptions-item label="关联标识">{{ detail.relatedBusinessId || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">正文</el-divider>
        <div class="content-box">{{ detail.noticeContent }}</div>
      </template>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { queryNoticeDetailApi } from '@/api/modules/notice';
import { useAuthStore } from '@/store/modules/auth';
import { useNoticeStore } from '@/store/modules/notice';
import type { IdValue, NoticeDetailData } from '@/types/api';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const noticeStore = useNoticeStore();

const canViewNotice = computed(() => authStore.permissionCodes.includes('notice:view'));
const loading = ref(false);
const detail = ref<NoticeDetailData | null>(null);

const noticeId = computed(() => {
  const rawId = String(route.params.id || '').trim();
  return rawId ? rawId : null;
});

function noticeTypeLabel(type?: string): string {
  if (type === 'SYSTEM') return '系统通知';
  if (type === 'BUSINESS') return '业务通知';
  return type || '-';
}

function noticeLevelLabel(level?: string): string {
  if (level === 'URGENT') return '紧急';
  if (level === 'NORMAL') return '普通';
  return level || '-';
}

function relatedBusinessTypeLabel(type?: string): string {
  if (type === 'PLAN_PUBLISHED') return '实习计划发布';
  if (type === 'APPLICATION_REVIEWED') return '申请审核';
  if (type === 'ASSIGNMENT_COMPLETED') return '实习分配';
  if (type === 'MATERIAL_EVALUATED') return '材料评价';
  if (type === 'MATERIAL_DEADLINE') return '材料截止提醒';
  if (type === 'SCORE_PUBLISHED') return '成绩发布';
  if (type === 'USER_REGISTER_SUBMITTED') return '注册申请待审核';
  return type || '-';
}

function goBack() {
  router.push('/notice');
}

async function fetchDetail(markUnreadAsRead = true) {
  if (!noticeId.value) {
    ElMessage.error('无效的消息标识');
    detail.value = null;
    return;
  }

  loading.value = true;
  try {
    const resp = await queryNoticeDetailApi(noticeId.value);
    detail.value = resp.data;

    if (markUnreadAsRead && resp.data.readFlag === 0) {
      await noticeStore.markRead(noticeId.value);
      const refreshed = await queryNoticeDetailApi(noticeId.value);
      detail.value = refreshed.data;
    }
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  if (!canViewNotice.value) {
    return;
  }
  fetchDetail();
});
</script>

<style scoped lang="scss">
.notice-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-weight: 600;
}

.content-box {
  min-height: 120px;
  line-height: 1.7;
  white-space: pre-wrap;
  background: #fafafa;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 12px;
}
</style>
