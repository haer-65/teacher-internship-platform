<template>
  <div class="evaluation-detail-page">
    <el-card v-loading="loading">
      <template #header>
        <div class="header-row">
          <span class="title">材料详情与过程评价</span>
          <el-button @click="goBack">返回待评价列表</el-button>
        </div>
      </template>

      <el-descriptions :column="3" border class="info-block" v-if="detail">
        <el-descriptions-item label="计划">{{ detail.planName }} ({{ detail.planCode }})</el-descriptions-item>
        <el-descriptions-item label="学生">{{ detail.studentName }} ({{ detail.studentNo }})</el-descriptions-item>
        <el-descriptions-item label="材料类型">{{ detail.materialTypeName }}</el-descriptions-item>
        <el-descriptions-item label="版本号">第{{ detail.materialVersionNo }}版</el-descriptions-item>
        <el-descriptions-item label="文件">{{ detail.fileName }}</el-descriptions-item>
        <el-descriptions-item label="提交时间">{{ detail.submittedTime || '-' }}</el-descriptions-item>
      </el-descriptions>

      <div class="file-actions" v-if="detail">
        <el-button type="primary" @click="previewFile">在线预览</el-button>
        <el-button type="success" @click="downloadFile">下载原件</el-button>
      </div>

      <el-divider content-position="left">历史评价记录（过程评价）</el-divider>
      <el-table :data="detail?.records || []" border>
        <el-table-column prop="recordNo" label="记录序号" width="100" />
        <el-table-column prop="evaluatorRole" label="评价角色" width="130" />
        <el-table-column prop="evaluatorName" label="评价教师" width="120" />
        <el-table-column prop="score" label="评分" width="90" />
        <el-table-column prop="commentText" label="评语" min-width="220" />
        <el-table-column prop="evaluatedTime" label="评价时间" width="170" />
      </el-table>

      <el-divider content-position="left">提交新评价</el-divider>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="submit-form">
        <el-form-item label="总评分" prop="score">
          <el-input-number
            v-model="scoreModel"
            :min="0"
            :max="100"
            :precision="2"
            :step="1"
            :disabled="hasScoreItems"
            style="width: 220px"
          />
          <span v-if="hasScoreItems" class="score-hint">已按分项自动计算</span>
        </el-form-item>

        <el-form-item label="评语" prop="commentText">
          <el-input
            v-model.trim="form.commentText"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入指导意见"
          />
        </el-form-item>

        <el-form-item label="分项评分">
          <div class="score-items-wrap">
            <div v-for="(item, index) in form.scoreItems" :key="index" class="score-item-row">
              <el-input v-model.trim="item.itemName" placeholder="分项名称" style="width: 180px" />
              <el-input-number v-model="item.itemScore" :min="0" :max="100" :precision="2" placeholder="分项得分" />
              <el-input-number v-model="item.itemWeight" :min="0" :max="100" :precision="2" placeholder="权重(%)" />
              <el-button type="danger" link @click="removeScoreItem(index)">删除</el-button>
            </div>
            <el-button type="primary" link @click="addScoreItem">+ 添加分项</el-button>
            <div v-if="hasScoreItems" class="score-note">
              分项已启用时，系统按分项得分和权重自动计算总评分，权重总和必须等于 100%
            </div>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button
            v-permission="'evaluation:submit'"
            type="primary"
            :loading="submitting"
            :disabled="!detail?.canEvaluate"
            @click="submitEvaluation"
          >
            提交过程评价
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import {
  getTeacherProcessDetailApi,
  submitProcessEvaluationApi
} from '@/api/modules/evaluation';
import {
  fetchMaterialDownloadBlobApi,
  fetchMaterialPreviewBlobApi
} from '@/api/modules/material';
import type { ProcessEvaluationDetailData } from '@/types/api';
import { normalizeRouteId } from '@/utils/id';
import { openMaterialVersionPreview } from '@/utils/material-preview';
import { calculateWeightedScore } from '@/utils/evaluation-score';

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const submitting = ref(false);
const detail = ref<ProcessEvaluationDetailData | null>(null);

const formRef = ref<FormInstance>();
const form = reactive({
  score: undefined as number | undefined,
  commentText: '',
  scoreItems: [] as Array<{ itemName: string; itemScore: number | undefined; itemWeight: number | undefined }>
});

const hasScoreItems = computed(() => form.scoreItems.length > 0);
const calculatedScore = computed(() =>
  calculateWeightedScore(
    form.scoreItems.map((item) => ({
      itemScore: item.itemScore,
      itemWeight: item.itemWeight
    }))
  )
);
const scoreModel = computed<number | undefined>({
  get: () => (hasScoreItems.value ? calculatedScore.value : form.score),
  set: (value) => {
    if (!hasScoreItems.value) {
      form.score = value;
    }
  }
});

const rules: FormRules = {
  score: [
    {
      validator: (_rule, value, callback) => {
        if (hasScoreItems.value) {
          if (calculatedScore.value === undefined) {
            callback(new Error('请完整填写分项评分和权重，且权重总和必须为100%'));
            return;
          }
          callback();
          return;
        }
        if (value === undefined || value === null) {
          callback(new Error('请输入总评分'));
          return;
        }
        callback();
      },
      trigger: ['change', 'blur']
    }
  ]
};

watch(
  [hasScoreItems, calculatedScore],
  () => {
    if (hasScoreItems.value) {
      form.score = calculatedScore.value;
    }
  },
  { immediate: true }
);

async function fetchDetail() {
  const materialVersionId = normalizeRouteId(route.params.materialVersionId);
  if (!materialVersionId) {
    ElMessage.error('缺少 materialVersionId 参数');
    return;
  }
  loading.value = true;
  try {
    const resp = await getTeacherProcessDetailApi(materialVersionId);
    detail.value = resp.data;
  } finally {
    loading.value = false;
  }
}

function addScoreItem() {
  form.scoreItems.push({
    itemName: '',
    itemScore: undefined,
    itemWeight: undefined
  });
}

function removeScoreItem(index: number) {
  form.scoreItems.splice(index, 1);
}

async function submitEvaluation() {
  if (!detail.value) {
    return;
  }
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  const invalidItem = form.scoreItems.find((item) => !item.itemName || item.itemScore === undefined || item.itemScore === null);
  if (invalidItem) {
    ElMessage.warning('请补全分项名称和分项得分');
    return;
  }
  if (hasScoreItems.value && calculatedScore.value === undefined) {
    ElMessage.warning('请补全分项得分和权重，且权重总和必须为 100%');
    return;
  }

  submitting.value = true;
  try {
    await submitProcessEvaluationApi({
      materialVersionId: detail.value.materialVersionId,
      score: hasScoreItems.value ? Number(calculatedScore.value) : Number(form.score),
      commentText: form.commentText || undefined,
      scoreItems: form.scoreItems.map((item) => ({
        itemName: item.itemName,
        itemScore: Number(item.itemScore),
        itemWeight: item.itemWeight === undefined || item.itemWeight === null ? undefined : Number(item.itemWeight)
      }))
    });
    ElMessage.success('过程评价提交成功');
    resetForm();
    fetchDetail();
  } finally {
    submitting.value = false;
  }
}

async function previewFile() {
  if (!detail.value) {
    return;
  }
  await openMaterialVersionPreview(
    {
      id: detail.value.materialVersionId,
      fileExt: detail.value.fileExt
    },
    () => fetchMaterialPreviewBlobApi(detail.value!.materialVersionId)
  );
}

async function downloadFile() {
  if (!detail.value) {
    return;
  }
  const blob = await fetchMaterialDownloadBlobApi(detail.value.materialVersionId);
  const objectUrl = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = objectUrl;
  a.download = detail.value.fileName || `材料_v${detail.value.materialVersionNo}`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(objectUrl);
}

function resetForm() {
  form.score = undefined;
  form.commentText = '';
  form.scoreItems = [];
}

function goBack() {
  router.push('/evaluation/teacher/pending');
}

onMounted(() => {
  fetchDetail();
});
</script>

<style scoped lang="scss">
.evaluation-detail-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-weight: 600;
}

.info-block {
  margin-bottom: 12px;
}

.file-actions {
  margin-bottom: 8px;
  display: flex;
  gap: 8px;
}

.submit-form {
  margin-top: 8px;
}

.score-items-wrap {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.score-item-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.score-hint {
  margin-left: 12px;
  color: var(--el-color-primary);
  font-size: 12px;
}

.score-note {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  line-height: 1.6;
}
</style>
