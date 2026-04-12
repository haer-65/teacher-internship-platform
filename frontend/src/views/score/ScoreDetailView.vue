<template>
  <div class="score-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="header-row">
          <span class="title">成绩详情</span>
          <div class="header-actions">
            <el-button @click="goBack">返回</el-button>
          </div>
        </div>
      </template>

      <el-descriptions v-if="detail" :column="3" border>
        <el-descriptions-item label="实习计划">{{ detail.planName }} ({{ detail.planCode }})</el-descriptions-item>
        <el-descriptions-item label="学生">{{ detail.studentName }} ({{ detail.studentNo }})</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status === 'PUBLISHED' ? '已发布' : '草稿' }}</el-descriptions-item>
        <el-descriptions-item label="学院">{{ detail.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ detail.majorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年级">{{ detail.gradeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="过程分">{{ showScore(detail.processScore) }}</el-descriptions-item>
        <el-descriptions-item label="综合分">{{ showScore(detail.finalScore) }}</el-descriptions-item>
        <el-descriptions-item label="自动总分">{{ showScore(detail.autoTotalScore) }}</el-descriptions-item>
        <el-descriptions-item label="总分">
          <el-tag type="success">{{ showScore(detail.totalScore) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="已调整">{{ detail.adjustedFlag === 1 ? '是' : '否' }}</el-descriptions-item>
        <el-descriptions-item label="调整次数">{{ detail.adjustTimes ?? 0 }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">材料明细</el-divider>
      <el-table :data="detail?.materialDetails || []" border>
        <el-table-column prop="materialTypeName" label="材料类型" min-width="170" />
        <el-table-column prop="materialWeight" label="权重(%)" width="90" />
        <el-table-column prop="innerTeacherProcessScore" label="校内导师分" width="100" />
        <el-table-column prop="baseTeacherProcessScore" label="基地导师分" width="100" />
        <el-table-column prop="materialCompositeScore" label="合成分" width="100" />
        <el-table-column prop="weightedContribution" label="折算贡献" width="110" />
        <el-table-column prop="materialVersionNo" label="版本" width="90">
          <template #default="{ row }">{{ row.materialVersionNo ? `第${row.materialVersionNo}版` : '-' }}</template>
        </el-table-column>
        <el-table-column prop="fileName" label="文件名" min-width="220" />
      </el-table>

      <el-divider content-position="left">计算公式</el-divider>
      <el-alert :title="detail?.formulaText || '-'" type="info" :closable="false" show-icon />
    </el-card>

  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getScoreDetailApi } from '@/api/modules/score';
import type { ScoreDetailData } from '@/types/api';
import { normalizeRouteId } from '@/utils/id';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const detail = ref<ScoreDetailData>();

async function fetchDetail() {
  const scoreSheetId = normalizeRouteId(route.params.id);
  if (!scoreSheetId) {
    return;
  }
  loading.value = true;
  try {
    const resp = await getScoreDetailApi(scoreSheetId);
    detail.value = resp.data;
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push('/score/list');
}

function showScore(value?: number): string {
  if (value === undefined || value === null || Number.isNaN(value)) {
    return '0.00';
  }
  return Number(value).toFixed(2);
}

onMounted(() => {
  fetchDetail();
});
</script>

<style scoped lang="scss">
.score-detail-page {
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

.header-actions {
  display: flex;
  gap: 8px;
}
</style>
