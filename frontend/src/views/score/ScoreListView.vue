<template>
  <div class="score-list-page">
    <el-card>
      <template #header>
        <div class="header-row">
          <span class="title">{{ pageTitle }}</span>
          <div v-if="showManageActions" class="header-actions">
            <el-button
              v-permission="'score:publish'"
              type="primary"
              :disabled="!currentPlanId"
              @click="handleRecalculate"
            >
              重新计算
            </el-button>
            <el-button
              v-permission="'score:publish'"
              type="warning"
              :disabled="!currentPlanId"
              @click="goPublishPage"
            >
              进入发布页
            </el-button>
            <el-button @click="handleExportExcel">导出表格</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="query" class="query-form score-query-form">
        <el-form-item label="实习计划">
          <el-select
            v-model="query.planId"
            filterable
            clearable
            placeholder="请选择实习计划"
            style="width: 320px"
          >
            <el-option
              v-for="option in planOptions"
              :key="String(option.id)"
              :label="option.name"
              :value="String(option.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model.trim="query.studentNo" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model.trim="query.studentName" clearable style="width: 160px" />
        </el-form-item>
        <el-form-item v-if="showManageActions" label="状态">
          <el-select v-model="query.status" clearable style="width: 140px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
          </el-select>
        </el-form-item>
        <el-form-item label="关键词">
          <el-input
            v-model.trim="query.keyword"
            clearable
            placeholder="计划/学生"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table
        ref="scoreTableRef"
        v-loading="loading"
        :data="records"
        border
        row-key="id"
        @selection-change="handleScoreSelectionChange"
      >
        <el-table-column v-if="showManageActions" type="selection" width="52" />
        <el-table-column prop="planCode" label="计划编码" width="130" />
        <el-table-column prop="planName" label="计划名称" min-width="170" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="processScore" label="过程分" width="90" />
        <el-table-column prop="finalScore" label="综合分" width="90" />
        <el-table-column prop="autoTotalScore" label="自动总分" width="100" />
        <el-table-column prop="totalScore" label="总分" width="90" />
        <el-table-column v-if="showManageActions" prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="showManageActions" label="已调整" width="90">
          <template #default="{ row }">
            <el-tag :type="row.adjustedFlag === 1 ? 'warning' : 'info'">
              {{ row.adjustedFlag === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="showManageActions" prop="adjustTimes" label="调整次数" width="100" />
        <el-table-column label="操作" :width="showManageActions ? 220 : 160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
            <el-button
              v-if="showManageActions"
              v-permission="'score:adjust'"
              link
              type="warning"
              :disabled="row.status === 'PUBLISHED' && row.adjustedFlag === 1"
              @click="openAdjustDialog(row.id, row.totalScore)"
            >
              调整
            </el-button>
            <el-button link type="success" @click="downloadPdf(row.id)">下载成绩单</el-button>
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

    <score-adjust-dialog
      v-model="adjustDialogVisible"
      :score-sheet-id="adjustSheetId"
      :current-score="adjustCurrentScore"
      @success="fetchData"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import {
  downloadScoreExcelApi,
  downloadScorePdfApi,
  queryScorePlanOptionsApi,
  queryScorePageApi,
  recalculateScorePlanApi
} from '@/api/modules/score';
import { useAuthStore } from '@/store/modules/auth';
import type { IdValue, ScoreListItem } from '@/types/api';
import ScoreAdjustDialog from './components/ScoreAdjustDialog.vue';
import { normalizeRouteId } from '@/utils/id';

const router = useRouter();
const authStore = useAuthStore();

const loading = ref(false);
const records = ref<ScoreListItem[]>([]);
const total = ref(0);
const planOptions = ref<Array<{ id: IdValue; name: string }>>([]);
const currentPlanId = ref<IdValue>();
const scoreTableRef = ref();
const scoreSelectionSyncing = ref(false);

const showManageActions = computed(() =>
  ['DEPT_ADMIN', 'SYS_ADMIN'].includes(authStore.currentRoleCode)
);

const pageTitle = computed(() => (showManageActions.value ? '成绩管理' : '成绩查看'));

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  planId: '' as string,
  studentNo: '',
  studentName: '',
  status: ''
});

const adjustDialogVisible = ref(false);
const adjustSheetId = ref<IdValue>();
const adjustCurrentScore = ref<number>();

async function loadPlanOptions() {
  try {
    const resp = await queryScorePlanOptionsApi();
    planOptions.value = resp.data || [];
  } catch {
    planOptions.value = [];
  }
}

async function fetchData() {
  const planId = normalizeRouteId(query.planId);
  loading.value = true;
  try {
    const resp = await queryScorePageApi({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      planId: planId || undefined,
      studentNo: query.studentNo || undefined,
      studentName: query.studentName || undefined,
      status: showManageActions.value ? query.status || undefined : 'PUBLISHED'
    });
    records.value = resp.data.records || [];
    total.value = Number(resp.data.total || 0);
  } catch {
    records.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

function handleSizeChange(size: number) {
  query.size = size;
  query.page = 1;
  fetchData();
}

function handleCurrentChange(page: number) {
  query.page = page;
  fetchData();
}

function handleReset() {
  query.page = 1;
  query.size = 10;
  query.keyword = '';
  query.planId = '';
  query.studentNo = '';
  query.studentName = '';
  query.status = '';
  if (showManageActions.value) {
    currentPlanId.value = undefined;
    scoreSelectionSyncing.value = true;
    try {
      scoreTableRef.value?.clearSelection();
    } finally {
      scoreSelectionSyncing.value = false;
    }
  }
  fetchData();
}

function goDetail(scoreSheetId: IdValue) {
  router.push(`/score/detail/${scoreSheetId}`);
}

function goPublishPage() {
  const planId = normalizeRouteId(currentPlanId.value);
  if (!planId) {
    ElMessage.warning('请先选择当前操作计划');
    return;
  }
  const currentPlan = records.value.find((item) => normalizeRouteId(item.planId) === planId);
  if (currentPlan?.status === 'PUBLISHED') {
    ElMessage.warning('该计划的成绩已发布，暂不允许再次发布');
    return;
  }
  router.push({
    path: '/score/publish',
    query: {
      planId
    }
  });
}

async function handleRecalculate() {
  const planId = normalizeRouteId(currentPlanId.value);
  if (!planId) {
    ElMessage.warning('请先选择当前操作计划');
    return;
  }
  await recalculateScorePlanApi(planId);
  ElMessage.success('重新计算完成');
  fetchData();
}

function applyCurrentPlan(row?: ScoreListItem) {
  if (!row) {
    currentPlanId.value = undefined;
    return;
  }
  currentPlanId.value = row.planId;
}

function handleScoreSelectionChange(rows: ScoreListItem[]) {
  if (!showManageActions.value) {
    return;
  }
  if (scoreSelectionSyncing.value) {
    return;
  }
  if (rows.length <= 0) {
    applyCurrentPlan(undefined);
    return;
  }
  if (rows.length === 1) {
    applyCurrentPlan(rows[0]);
    return;
  }
  const latest = rows[rows.length - 1];
  scoreSelectionSyncing.value = true;
  try {
    scoreTableRef.value?.clearSelection();
    scoreTableRef.value?.toggleRowSelection(latest, true);
    applyCurrentPlan(latest);
  } finally {
    scoreSelectionSyncing.value = false;
  }
}

function openAdjustDialog(scoreSheetId: IdValue, currentScore?: number) {
  adjustSheetId.value = scoreSheetId;
  adjustCurrentScore.value = currentScore;
  adjustDialogVisible.value = true;
}

async function handleExportExcel() {
  if (!showManageActions.value) {
    return;
  }
  const blob = await downloadScoreExcelApi({
    planId: normalizeRouteId(query.planId) || undefined,
    status: query.status || undefined
  });
  const objectUrl = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = objectUrl;
  a.download = `成绩单导出_${Date.now()}.xlsx`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(objectUrl);
}

async function downloadPdf(scoreSheetId: IdValue) {
  const blob = await downloadScorePdfApi(scoreSheetId);
  const objectUrl = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = objectUrl;
  a.download = `成绩单_${scoreSheetId}.pdf`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(objectUrl);
}

onMounted(() => {
  void loadPlanOptions();
  void fetchData();
});
</script>

<style scoped lang="scss">
.score-list-page {
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
  align-items: center;
}

.query-form {
  margin-bottom: 12px;
}

.score-query-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 12px 20px;
}

.score-query-form :deep(.el-form-item) {
  margin: 0;
}

.score-query-form :deep(.el-form-item__content) {
  min-width: 0;
}

.pager-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
