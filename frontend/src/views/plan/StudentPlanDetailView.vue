<template>
  <el-card v-loading="loading">
    <template #header>
      <div class="header-row">
        <span class="title">计划详情</span>
        <el-button @click="goBack">返回</el-button>
      </div>
    </template>

    <template v-if="detail">
      <el-alert
        :title="applyStatusTitle"
        :type="applyStatusType"
        :description="applyStatusDescription"
        :closable="false"
        show-icon
        class="apply-status-alert"
      />

      <el-descriptions :column="2" border>
        <el-descriptions-item label="计划编码">{{ detail.planCode }}</el-descriptions-item>
        <el-descriptions-item label="计划名称">{{ detail.planName }}</el-descriptions-item>
        <el-descriptions-item label="学年">{{ detail.academicYear }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ detail.term }}</el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="申请截止时间">{{ detail.applyDeadline }}</el-descriptions-item>
        <el-descriptions-item label="学生名额">{{ detail.studentQuota }}</el-descriptions-item>
        <el-descriptions-item label="校内导师权重">{{ detail.innerTeacherWeight }}%</el-descriptions-item>
        <el-descriptions-item label="基地导师权重">{{ detail.baseTeacherWeight }}%</el-descriptions-item>
        <el-descriptions-item label="说明" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>本计划可选实习基地</el-divider>
      <el-table :data="detail.planBases || []" border>
        <el-table-column prop="baseCode" label="基地编码" width="140" />
        <el-table-column prop="baseName" label="基地名称" min-width="220" />
        <el-table-column prop="baseQuota" label="基地名额" width="120" />
        <el-table-column prop="remark" label="备注" min-width="180" />
      </el-table>

      <el-divider>材料类型配置</el-divider>
      <el-table :data="detail.materialTypes || []" border>
        <el-table-column prop="typeCode" label="类型编码" width="130" />
        <el-table-column prop="typeName" label="类型名称" min-width="140" />
        <el-table-column label="必交" width="90">
          <template #default="{ row }">{{ row.requiredFlag === 1 ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="deadlineTime" label="截止时间" min-width="170" />
        <el-table-column prop="weight" label="权重(%)" width="110" />
        <el-table-column label="允许补交" width="120">
          <template #default="{ row }">{{ row.allowResubmit ? '是' : '否' }}</template>
        </el-table-column>
        <el-table-column prop="maxSubmitCount" label="最大提交次数" width="120" />
      </el-table>

      <el-divider>附件</el-divider>
      <plan-attachment-uploader :plan-id="detail.id" :attachments="detail.attachments || []" :readonly="true" />
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PlanAttachmentUploader from './components/PlanAttachmentUploader.vue';
import { ElMessage } from 'element-plus';
import { getStudentPlanDetailApi } from '@/api/modules/plan';
import type { PlanDetailData } from '@/types/api';
import { normalizeRouteId } from '@/utils/id';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const detail = ref<PlanDetailData | null>(null);

const canApply = computed(() => {
  if (!detail.value) {
    return false;
  }
  if (detail.value.planStatus !== 'PUBLISHED') {
    return false;
  }
  if (!detail.value.applyDeadline) {
    return false;
  }
  return new Date(detail.value.applyDeadline).getTime() >= Date.now();
});

const applyStatusTitle = computed(() => {
  if (!detail.value) {
    return '';
  }
  if (canApply.value) {
    return '当前计划可以申请';
  }
  if (detail.value.planStatus === 'PUBLISHED') {
    return '当前计划已截止申请';
  }
  if (detail.value.planStatus === 'FINISHED') {
    return '当前计划已结束';
  }
  if (detail.value.planStatus === 'ARCHIVED') {
    return '当前计划已归档';
  }
  return '当前计划暂不可申请';
});

const applyStatusType = computed<'success' | 'warning' | 'info'>(() => {
  if (!detail.value) {
    return 'info';
  }
  if (canApply.value) {
    return 'success';
  }
  if (detail.value.planStatus === 'PUBLISHED') {
    return 'warning';
  }
  return 'info';
});

const applyStatusDescription = computed(() => {
  if (!detail.value) {
    return '';
  }
  if (canApply.value) {
    return `请在 ${detail.value.applyDeadline} 前完成志愿填报并提交申请。`;
  }
  if (detail.value.planStatus === 'PUBLISHED') {
    return `该计划的申请截止时间为 ${detail.value.applyDeadline}，当前已不能再提交申请。`;
  }
  if (detail.value.planStatus === 'FINISHED') {
    return '该计划的实习流程已结束，仅支持查看计划内容，不再开放申请。';
  }
  if (detail.value.planStatus === 'ARCHIVED') {
    return '该计划已归档，仅供历史查看，不再开放申请。';
  }
  return '当前计划还未开放申请，请以计划状态和发布时间为准。';
});

async function fetchDetail() {
  const id = normalizeRouteId(route.params.id);
  if (!id) {
    ElMessage.error('无效的计划标识');
    router.replace('/plan/student');
    return;
  }
  loading.value = true;
  try {
    const resp = await getStudentPlanDetailApi(id);
    detail.value = resp.data;
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push('/plan/student');
}

onMounted(() => {
  fetchDetail();
});
</script>

<style scoped lang="scss">
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-weight: 600;
}

.apply-status-alert {
  margin-bottom: 16px;
}
</style>
