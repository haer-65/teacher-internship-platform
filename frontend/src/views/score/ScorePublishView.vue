<template>
  <div class="score-publish-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span class="title">成绩发布</span>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="form" class="query-form">
        <el-form-item label="计划标识">
          <el-input v-model.trim="form.planId" placeholder="è¯·è¾“å…¥è®¡åˆ’ç¼–å·" style="width: 160px" />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="form.recalculate">预览前重新计算</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button
            v-permission="'score:publish'"
            type="primary"
            :loading="previewLoading"
            @click="handlePreview"
          >
            预览
          </el-button>
        </el-form-item>
        <el-form-item>
          <el-button
            v-permission="'score:publish'"
            type="success"
            :loading="publishing"
            :disabled="!previewData || !previewData.total || !previewData.canPublish"
            @click="handlePublish"
          >
            发布成绩
          </el-button>
        </el-form-item>
      </el-form>

      <el-descriptions v-if="previewData" :column="3" border style="margin-bottom: 12px">
        <el-descriptions-item label="实习计划">{{ previewData.planName }} ({{ previewData.planCode }})</el-descriptions-item>
        <el-descriptions-item label="计划状态">{{ planStatusLabel(previewData.planStatus) }}</el-descriptions-item>
        <el-descriptions-item label="成绩发布状态">{{ publishStatusLabel(previewData.scorePublishStatus, previewData.canPublish) }}</el-descriptions-item>
        <el-descriptions-item label="草稿成绩单数">{{ previewData.total }}</el-descriptions-item>
      </el-descriptions>

      <el-alert
        v-if="previewData && previewData.canPublish === false && previewData.publishBlockedReason"
        :title="previewData.publishBlockedReason"
        type="warning"
        show-icon
        :closable="false"
        style="margin-bottom: 12px"
      />

      <el-table v-loading="previewLoading" :data="previewData?.records || []" border>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="processScore" label="过程分" width="90" />
        <el-table-column prop="finalScore" label="综合分" width="90" />
        <el-table-column prop="autoTotalScore" label="自动总分" width="100" />
        <el-table-column prop="totalScore" label="总分" width="90" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { previewScorePublishApi, publishScorePlanApi } from '@/api/modules/score';
import type { ScorePublishPreviewData } from '@/types/api';
import { normalizeRouteId } from '@/utils/id';

const route = useRoute();
const router = useRouter();

const previewLoading = ref(false);
const publishing = ref(false);
const previewData = ref<ScorePublishPreviewData>();

const form = reactive({
  planId: '' as string,
  recalculate: true
});

async function handlePreview() {
  const planId = normalizeRouteId(form.planId);
  if (!planId) {
    ElMessage.warning('请输入计划标识');
    return;
  }
  previewLoading.value = true;
  try {
    const resp = await previewScorePublishApi(planId, form.recalculate ? 1 : 0);
    previewData.value = resp.data;
  } finally {
    previewLoading.value = false;
  }
}

async function handlePublish() {
  const planId = normalizeRouteId(form.planId);
  if (!planId) {
    ElMessage.warning('请输入计划标识');
    return;
  }
  publishing.value = true;
  try {
    const resp = await publishScorePlanApi(planId);
    ElMessage.success(`已发布 ${resp.data.publishedCount} 条成绩单`);
    handlePreview();
  } finally {
    publishing.value = false;
  }
}

function planStatusLabel(status?: string): string {
  switch (status) {
    case 'DRAFT':
      return '草稿';
    case 'PUBLISHED':
      return '已发布';
    case 'FINISHED':
      return '已结束';
    case 'ARCHIVED':
      return '已归档';
    default:
      return status || '-';
  }
}

function publishStatusLabel(status?: string, canPublish?: boolean): string {
  if (canPublish === false) {
    return '待综合评价完成';
  }
  switch (status) {
    case 'PUBLISHED':
      return '已发布';
    case 'UNPUBLISHED':
      return '未发布';
    default:
      return status || '-';
  }
}

function goBack() {
  router.push('/score/list');
}

onMounted(() => {
  const planId = normalizeRouteId(route.query.planId);
  if (planId) {
    form.planId = planId;
    handlePreview();
  }
});
</script>

<style scoped lang="scss">
.score-publish-page {
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

.query-form {
  margin-bottom: 12px;
}
</style>
