<template>
  <div class="evaluation-final-page">
    <el-card>
      <template #header>
        <div class="page-header">
          <span class="title">综合评价</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model.trim="query.keyword" clearable placeholder="计划/学生" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model.trim="query.studentNo" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="listLoading" :data="records" border @row-click="handleSelectAssignment">
        <el-table-column prop="planCode" label="计划编码" width="150" />
        <el-table-column prop="planName" label="计划名称" min-width="170" />
        <el-table-column prop="planStatus" label="计划状态" width="120" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="myEvaluationCount" label="我的评价次数" width="120" />
        <el-table-column label="可评价" width="90">
          <template #default="{ row }">
            <el-tag :type="row.canEvaluate ? 'success' : 'info'">{{ row.canEvaluate ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click.stop="loadDetail(row.assignmentId)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :current-page="query.page"
          :page-size="query.size"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <el-card v-if="detail" v-loading="detailLoading">
      <template #header>
        <span class="title">综合评价详情 - {{ detail.studentName }} ({{ detail.studentNo }})</span>
      </template>

      <el-descriptions :column="3" border class="info-block">
        <el-descriptions-item label="计划">{{ detail.planName }} ({{ detail.planCode }})</el-descriptions-item>
        <el-descriptions-item label="计划状态">{{ detail.planStatus }}</el-descriptions-item>
        <el-descriptions-item label="可提交">{{ detail.canEvaluate ? '是' : '否' }}</el-descriptions-item>
      </el-descriptions>

      <el-table :data="detail.records || []" border>
        <el-table-column prop="recordNo" label="记录序号" width="100" />
        <el-table-column prop="evaluatorRole" label="评价角色" width="130" />
        <el-table-column prop="evaluatorName" label="评价教师" width="120" />
        <el-table-column prop="score" label="评分" width="90" />
        <el-table-column prop="commentText" label="综合意见" min-width="220" />
        <el-table-column prop="evaluatedTime" label="评价时间" width="170" />
      </el-table>

      <el-divider content-position="left">提交综合评价</el-divider>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" class="submit-form">
        <el-form-item label="综合评分" prop="score">
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

        <el-form-item label="综合意见" prop="commentText">
          <el-input
            v-model.trim="form.commentText"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入综合评价意见"
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
            :disabled="!detail.canEvaluate"
            @click="submitFinalEvaluation"
          >
            提交综合评价
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
  getTeacherFinalDetailApi,
  queryTeacherFinalPendingPageApi,
  submitFinalEvaluationApi
} from '@/api/modules/evaluation';
import type { FinalEvaluationDetailData, IdValue, TeacherFinalPendingItem } from '@/types/api';
import { normalizeRouteId } from '@/utils/id';
import { calculateWeightedScore } from '@/utils/evaluation-score';

const route = useRoute();
const router = useRouter();

const listLoading = ref(false);
const detailLoading = ref(false);
const submitting = ref(false);

const records = ref<TeacherFinalPendingItem[]>([]);
const total = ref(0);
const detail = ref<FinalEvaluationDetailData | null>(null);

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  studentNo: ''
});

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
          callback(new Error('请输入综合评分'));
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

async function fetchList() {
  listLoading.value = true;
  try {
    const resp = await queryTeacherFinalPendingPageApi({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      studentNo: query.studentNo || undefined,
      pendingOnly: undefined
    });
    records.value = resp.data.records || [];
    total.value = Number(resp.data.total || 0);
  } finally {
    listLoading.value = false;
  }
}

async function loadDetail(assignmentId: IdValue) {
  detailLoading.value = true;
  try {
    const resp = await getTeacherFinalDetailApi(assignmentId);
    detail.value = resp.data;
  } finally {
    detailLoading.value = false;
  }
}

function handleSelectAssignment(row: TeacherFinalPendingItem) {
  loadDetail(row.assignmentId);
}

function handleSizeChange(size: number) {
  query.size = size;
  query.page = 1;
  fetchList();
}

function handleCurrentChange(page: number) {
  query.page = page;
  fetchList();
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

async function submitFinalEvaluation() {
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
    await submitFinalEvaluationApi({
      assignmentId: detail.value.assignmentId,
      score: hasScoreItems.value ? Number(calculatedScore.value) : Number(form.score),
      commentText: form.commentText || undefined,
      scoreItems: form.scoreItems.map((item) => ({
        itemName: item.itemName,
        itemScore: Number(item.itemScore),
        itemWeight: item.itemWeight === undefined || item.itemWeight === null ? undefined : Number(item.itemWeight)
      }))
    });
    ElMessage.success('综合评价提交成功');
    resetForm();
    loadDetail(detail.value.assignmentId);
    fetchList();
  } finally {
    submitting.value = false;
  }
}

function resetForm() {
  form.score = undefined;
  form.commentText = '';
  form.scoreItems = [];
}

function goBack() {
  if (window.history.length > 1) {
    router.back();
    return;
  }
  router.push('/evaluation/teacher/pending');
}

onMounted(async () => {
  await fetchList();
  const assignmentId = normalizeRouteId(route.query.assignmentId);
  if (assignmentId) {
    loadDetail(assignmentId);
  }
});
</script>

<style scoped lang="scss">
.evaluation-final-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.title {
  font-weight: 600;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.query-form {
  margin-bottom: 12px;
}

.pager-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.info-block {
  margin-bottom: 12px;
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
