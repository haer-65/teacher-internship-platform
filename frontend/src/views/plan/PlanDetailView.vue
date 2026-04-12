<template>
  <el-card v-loading="loading">
    <template #header>
      <div class="header-row">
        <div class="title">计划详情</div>
        <div class="action-wrap">
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </div>
    </template>

    <template v-if="detail">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="计划编码">{{ detail.planCode }}</el-descriptions-item>
        <el-descriptions-item label="计划名称">{{ detail.planName }}</el-descriptions-item>
        <el-descriptions-item label="学年">{{ detail.academicYear }}</el-descriptions-item>
        <el-descriptions-item label="学期">{{ detail.term }}</el-descriptions-item>
        <el-descriptions-item label="学生名额">{{ detail.studentQuota }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusTagType(detail.planStatus)">{{ statusLabel(detail.planStatus) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="开始时间">{{ detail.startTime }}</el-descriptions-item>
        <el-descriptions-item label="结束时间">{{ detail.endTime }}</el-descriptions-item>
        <el-descriptions-item label="报名截止时间">{{ detail.applyDeadline }}</el-descriptions-item>
        <el-descriptions-item label="院系">{{ detail.deptName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="校内导师权重">{{ detail.innerTeacherWeight }}%</el-descriptions-item>
        <el-descriptions-item label="基地导师权重">{{ detail.baseTeacherWeight }}%</el-descriptions-item>
        <el-descriptions-item label="计划说明" :span="2">{{ detail.description || '-' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider>可选实习基地</el-divider>
      <el-table :data="detail.planBases || []" border>
        <el-table-column prop="baseCode" label="基地编码" width="140" />
        <el-table-column prop="baseName" label="基地名称" min-width="200" />
        <el-table-column prop="baseQuota" label="名额" width="120" />
        <el-table-column prop="sortNo" label="排序" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="180" />
      </el-table>

      <el-divider>材料类型配置</el-divider>
      <el-table :data="detail.materialTypes || []" border>
        <el-table-column prop="typeCode" label="类型编码" width="120" />
        <el-table-column prop="typeName" label="类型名称" min-width="140" />
        <el-table-column label="必交" width="90">
          <template #default="{ row }">
            <el-tag :type="row.requiredFlag === 1 ? 'success' : 'info'">{{ row.requiredFlag === 1 ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="deadlineTime" label="截止时间" width="170" />
        <el-table-column prop="weight" label="权重(%)" width="100" />
        <el-table-column label="允许补交" width="100">
          <template #default="{ row }">
            <el-tag :type="row.allowResubmit ? 'warning' : 'info'">{{ row.allowResubmit ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxSubmitCount" label="最大次数" width="100" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
              {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <el-divider>附件</el-divider>
      <plan-attachment-uploader :plan-id="detail.id" :attachments="detail.attachments || []" :readonly="true" />
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import PlanAttachmentUploader from './components/PlanAttachmentUploader.vue';
import { getPlanDetailApi } from '@/api/modules/plan';
import type { PlanDetailData, PlanStatus } from '@/types/api';
import { normalizeRouteId } from '@/utils/id';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const detail = ref<PlanDetailData>();

function statusLabel(status: PlanStatus) {
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
      return status;
  }
}

function statusTagType(status: PlanStatus) {
  switch (status) {
    case 'DRAFT':
      return 'info';
    case 'PUBLISHED':
      return 'success';
    case 'FINISHED':
      return 'warning';
    case 'ARCHIVED':
      return 'danger';
    default:
      return 'info';
  }
}

async function fetchDetail() {
  const id = normalizeRouteId(route.params.id);
  if (!id) {
    return;
  }
  loading.value = true;
  try {
    const resp = await getPlanDetailApi(id);
    detail.value = resp.data;
  } finally {
    loading.value = false;
  }
}

function goBack() {
  router.push('/plan/manage');
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
  font-size: 20px;
  font-weight: 600;
}

.action-wrap {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
}
</style>
