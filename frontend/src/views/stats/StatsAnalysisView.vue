<template>
  <div class="stats-page">
    <el-card class="filter-card">
      <div class="filter-head">
        <div class="panel-copy">
          <div class="panel-title">统计维度</div>
        </div>
        <div class="dimension-control">
          <el-radio-group
            v-model="comparisonEnabled"
            size="small"
            class="dimension-toggle"
            @change="handleComparisonToggle"
          >
            <el-radio-button :label="false">汇总</el-radio-button>
            <el-radio-button :label="true">对比</el-radio-button>
          </el-radio-group>
          <el-select
            v-if="comparisonEnabled"
            v-model="queryForm.dimension"
            clearable
            placeholder="选择对比维度"
            class="dimension-select"
            @change="handleDimensionChange"
          >
            <el-option label="学院" value="DEPARTMENT" />
            <el-option label="专业" value="MAJOR" />
            <el-option label="年级" value="GRADE" />
            <el-option label="实习基地" value="BASE" />
            <el-option label="指导教师" value="TEACHER" />
            <el-option label="实习计划" value="PLAN" />
          </el-select>
        </div>
      </div>

      <el-form :inline="true" :model="queryForm" class="filter-form stats-filter-form">
        <el-form-item label="学院">
          <el-select
            v-model="queryForm.deptId"
            clearable
            placeholder="选择"
            style="width: 180px"
            :disabled="isDeptAdmin"
            @change="handleDeptChange"
          >
            <el-option v-for="item in departmentOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="专业">
          <el-select v-model="queryForm.majorId" clearable placeholder="选择" style="width: 180px">
            <el-option v-for="item in majorOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="年级">
          <el-select v-model="queryForm.gradeId" clearable placeholder="选择" style="width: 180px">
            <el-option v-for="item in gradeOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="实习基地">
          <el-select v-model="queryForm.baseId" clearable placeholder="选择" style="width: 180px">
            <el-option v-for="item in baseOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="指导教师">
          <el-select v-model="queryForm.teacherId" clearable placeholder="选择" style="width: 180px">
            <el-option v-for="item in teacherOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="实习计划">
          <el-select v-model="queryForm.planId" clearable placeholder="选择" style="width: 180px">
            <el-option v-for="item in planOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>

        <el-form-item label="计划状态">
          <el-select v-model="queryForm.planStatus" clearable placeholder="全部" style="width: 180px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="已发布" value="PUBLISHED" />
            <el-option label="已结束" value="FINISHED" />
            <el-option label="已归档" value="ARCHIVED" />
          </el-select>
        </el-form-item>

        <el-form-item class="scope-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button type="success" @click="handleExport">导出当前筛选</el-button>
        </el-form-item>
      </el-form>

    </el-card>

    <el-row :gutter="16">
      <el-col v-for="card in metricCards" :key="card.label" :xs="24" :sm="12" :lg="6">
        <el-card class="metric-card">
          <div class="metric-label">{{ card.label }}</div>
          <div class="metric-value">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :xl="12">
        <el-card class="chart-card">
          <div class="chart-title">{{ overviewChartTitle }}</div>
          <div v-if="overviewChartHint" class="chart-hint">{{ overviewChartHint }}</div>
          <div ref="overviewChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="12">
        <el-card class="chart-card">
          <div class="chart-title">成绩分布</div>
          <div ref="scoreChartRef" class="chart-box"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card">
      <template #header>
        <div class="table-header">统计明细</div>
      </template>
      <el-table v-loading="tableLoading" :data="tableData" border>
        <el-table-column prop="dimensionName" label="维度名称" min-width="180" />
        <el-table-column prop="internshipStudentCount" label="实习人数" width="100" />
        <el-table-column label="材料提交率" width="120"><template #default="{ row }">{{ formatRate(row.materialSubmitRate) }}</template></el-table-column>
        <el-table-column label="逾期率" width="100"><template #default="{ row }">{{ formatRate(row.materialOverdueRate) }}</template></el-table-column>
        <el-table-column label="评价完成率" width="120"><template #default="{ row }">{{ formatRate(row.evaluationCompletionRate) }}</template></el-table-column>
        <el-table-column prop="averageScore" label="平均分" width="100" />
        <el-table-column label="优秀率" width="100"><template #default="{ row }">{{ formatRate(row.excellentRate) }}</template></el-table-column>
        <el-table-column label="及格率" width="100"><template #default="{ row }">{{ formatRate(row.passRate) }}</template></el-table-column>
      </el-table>
      <div class="pager-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :current-page="page"
          :page-size="size"
          :page-sizes="[10, 20, 50]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { downloadStatsExportApi, queryStatsDimensionPageApi, queryStatsFilterOptionsApi, queryStatsOverviewApi } from '@/api/modules/stats';
import { useAuthStore } from '@/store/modules/auth';
import type { IdValue, StatsDashboardData, StatsDimensionItem, StatsDimensionType, StatsOptionItem } from '@/types/api';

const authStore = useAuthStore();
const overviewChartRef = ref<HTMLDivElement>();
const scoreChartRef = ref<HTMLDivElement>();
let overviewChart: echarts.ECharts | null = null;
let scoreChart: echarts.ECharts | null = null;

const departmentOptions = ref<StatsOptionItem[]>([]);
const majorOptions = ref<StatsOptionItem[]>([]);
const gradeOptions = ref<StatsOptionItem[]>([]);
const baseOptions = ref<StatsOptionItem[]>([]);
const teacherOptions = ref<StatsOptionItem[]>([]);
const planOptions = ref<StatsOptionItem[]>([]);

const queryForm = reactive({
  dimension: '' as '' | StatsDimensionType,
  deptId: undefined as IdValue | undefined,
  majorId: undefined as IdValue | undefined,
  gradeId: undefined as IdValue | undefined,
  baseId: undefined as IdValue | undefined,
  teacherId: undefined as IdValue | undefined,
  planId: undefined as IdValue | undefined,
  planStatus: '' as '' | 'DRAFT' | 'PUBLISHED' | 'FINISHED' | 'ARCHIVED'
});

const comparisonEnabled = ref(false);
const isDeptAdmin = computed(() => authStore.currentRoleCode === 'DEPT_ADMIN');
const lockedDeptId = computed(() => {
  const value = authStore.userInfo?.deptId;
  return value === undefined || value === null ? undefined : String(value);
});
const summaryScopeLabel = computed(() => {
  if (isDeptAdmin.value) {
    return authStore.userInfo?.deptName || '本学院范围';
  }
  return '全校范围';
});
const committedView = reactive({
  teacherId: undefined as IdValue | undefined,
  dimension: '' as '' | StatsDimensionType,
  comparisonEnabled: false
});

const page = ref(1);
const size = ref(10);
const total = ref(0);
const tableLoading = ref(false);
const tableData = ref<StatsDimensionItem[]>([]);
const dashboard = ref<StatsDashboardData | null>(null);

const dimensionLabels: Record<Exclude<StatsDimensionType, 'SUMMARY'>, string> = {
  DEPARTMENT: '学院',
  MAJOR: '专业',
  GRADE: '年级',
  BASE: '实习基地',
  TEACHER: '指导教师',
  PLAN: '实习计划'
};

const metricCards = computed(() => {
  const overview = dashboard.value?.overview;
  const score = dashboard.value?.scoreSummary;
  return [
    { label: '实习人数', value: overview?.internshipStudentCount ?? 0 },
    { label: '材料提交率', value: formatRate(overview?.materialSubmitRate) },
    { label: '逾期率', value: formatRate(overview?.materialOverdueRate) },
    { label: '评价完成率', value: formatRate(overview?.evaluationCompletionRate) },
    { label: '平均分', value: score?.averageScore ?? 0 },
    { label: '优秀率', value: formatRate(score?.excellentRate) },
    { label: '及格率', value: formatRate(score?.passRate) },
    { label: '成绩人数', value: score?.scoreStudentCount ?? 0 }
  ];
});

function getDimensionLabel(dimension?: StatsDimensionType | '') {
  if (!dimension || dimension === 'SUMMARY') {
    return summaryScopeLabel.value;
  }
  return dimensionLabels[dimension];
}

const dimensionModeText = computed(() => (
  comparisonEnabled.value
    ? `已开启对比：${getDimensionLabel(queryForm.dimension)}`
    : `已关闭对比，按${summaryScopeLabel.value}汇总`
));
const overviewChartTitle = computed(() =>
  committedView.teacherId || (committedView.comparisonEnabled && committedView.dimension === 'TEACHER')
    ? '指导教师履职情况'
    : '材料与评价执行情况'
);
const overviewChartHint = computed(() =>
  committedView.teacherId || (committedView.comparisonEnabled && committedView.dimension === 'TEACHER')
    ? '材料看学生，评价看教师本人。'
    : ''
);

function buildQuery() {
  return {
    dimension: queryForm.dimension || 'SUMMARY',
    deptId: isDeptAdmin.value ? (queryForm.deptId ?? lockedDeptId.value) : queryForm.deptId,
    majorId: queryForm.majorId,
    gradeId: queryForm.gradeId,
    baseId: queryForm.baseId,
    teacherId: queryForm.teacherId,
    planId: queryForm.planId,
    planStatus: queryForm.planStatus || undefined
  };
}

function formatRate(value?: string | number) {
  if (value === undefined || value === null || value === '') return '0%';
  if (typeof value === 'number') return `${value}%`;
  return String(value).includes('%') ? String(value) : `${value}%`;
}

async function fetchOptions() {
  const resp = await queryStatsFilterOptionsApi(buildQuery());
  departmentOptions.value = resp.data.departments || [];
  majorOptions.value = resp.data.majors || [];
  gradeOptions.value = resp.data.grades || [];
  baseOptions.value = resp.data.bases || [];
  teacherOptions.value = resp.data.teachers || [];
  planOptions.value = resp.data.plans || [];
}

async function fetchOverview() {
  const resp = await queryStatsOverviewApi(buildQuery());
  dashboard.value = resp.data;
}

async function fetchTable() {
  tableLoading.value = true;
  try {
    const resp = await queryStatsDimensionPageApi({ ...buildQuery(), page: page.value, size: size.value });
    tableData.value = resp.data.records || [];
    total.value = Number(resp.data.total || 0);
  } finally {
    tableLoading.value = false;
  }
}

async function refreshAllData() {
  await Promise.all([fetchOptions(), fetchOverview(), fetchTable()]);
  committedView.teacherId = queryForm.teacherId;
  committedView.dimension = queryForm.dimension;
  committedView.comparisonEnabled = comparisonEnabled.value;
  await nextTick();
  renderCharts();
}

function toRate(value?: string | number) {
  if (typeof value === 'number') return value;
  if (!value) return 0;
  return Number(String(value).replace('%', '')) || 0;
}

function renderCharts() {
  if (overviewChartRef.value) {
    overviewChart = overviewChart || echarts.init(overviewChartRef.value);
    const rows = tableData.value.slice(0, 8);
    const categories = rows.length > 0
      ? rows.map((item) => item.dimensionName || '未命名')
      : ['提交率', '逾期率', '评价率'];
    const submitSeries = rows.length > 0
      ? rows.map((item) => toRate(item.materialSubmitRate))
      : [toRate(dashboard.value?.overview.materialSubmitRate), 0, toRate(dashboard.value?.overview.evaluationCompletionRate)];
    const overdueSeries = rows.length > 0
      ? rows.map((item) => toRate(item.materialOverdueRate))
      : [toRate(dashboard.value?.overview.materialOverdueRate), 0, 0];
    const evaluationSeries = rows.length > 0
      ? rows.map((item) => toRate(item.evaluationCompletionRate))
      : [toRate(dashboard.value?.overview.evaluationCompletionRate), 0, 0];
    overviewChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { top: 0 },
      grid: { left: 40, right: 20, top: 30, bottom: 30, containLabel: true },
      xAxis: { type: 'category', data: categories },
      yAxis: { type: 'value', max: 100 },
      series: [
        { name: '材料提交率', type: 'bar', data: submitSeries, itemStyle: { color: '#409EFF' } },
        { name: '逾期率', type: 'bar', data: overdueSeries, itemStyle: { color: '#F56C6C' } },
        { name: '评价完成率', type: 'bar', data: evaluationSeries, itemStyle: { color: '#67C23A' } }
      ]
    });
  }

  if (scoreChartRef.value) {
    scoreChart = scoreChart || echarts.init(scoreChartRef.value);
    scoreChart.setOption({
      tooltip: { trigger: 'item' },
      series: [{
        type: 'pie',
        radius: ['45%', '70%'],
        data: (dashboard.value?.scoreDistribution || []).map((item) => ({ name: item.bucketLabel, value: item.scoreCount }))
      }]
    });
  }
}

function applyRoleDefaults() {
  comparisonEnabled.value = false;
  queryForm.dimension = '';
  queryForm.deptId = isDeptAdmin.value ? lockedDeptId.value : undefined;
  queryForm.majorId = undefined;
  queryForm.gradeId = undefined;
  queryForm.baseId = undefined;
  queryForm.teacherId = undefined;
  queryForm.planId = undefined;
  queryForm.planStatus = '';
  page.value = 1;
}

async function handleComparisonToggle(enabled: boolean) {
  if (!enabled) {
    queryForm.dimension = '';
  } else if (!queryForm.dimension) {
    queryForm.dimension = 'DEPARTMENT';
  }
  page.value = 1;
  await refreshAllData();
}

async function handleDimensionChange() {
  page.value = 1;
  await refreshAllData();
}

async function handleDeptChange() {
  page.value = 1;
  await refreshAllData();
}

async function refreshPageData() {
  await refreshAllData();
}

async function handleSearch() {
  page.value = 1;
  await refreshAllData();
}

async function handleReset() {
  applyRoleDefaults();
  await refreshPageData();
}

function handleSizeChange(nextSize: number) {
  size.value = nextSize;
  page.value = 1;
  fetchTable();
}

function handleCurrentChange(nextPage: number) {
  page.value = nextPage;
  fetchTable();
}

async function handleExport() {
  const blob = await downloadStatsExportApi(buildQuery());
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = '统计分析导出.xlsx';
  link.click();
  window.URL.revokeObjectURL(url);
  ElMessage.success('导出成功');
}

function handleResize() {
  overviewChart?.resize();
  scoreChart?.resize();
}

watch(
  () => [authStore.currentRoleCode, authStore.userInfo?.deptId],
  async () => {
    applyRoleDefaults();
    await refreshPageData();
  }
);

watch(
  () => queryForm.dimension,
  (value) => {
    comparisonEnabled.value = Boolean(value);
  },
  { immediate: true }
);

onMounted(async () => {
  applyRoleDefaults();
  await refreshPageData();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  overviewChart?.dispose();
  scoreChart?.dispose();
});
</script>

<style scoped lang="scss">
.stats-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.filter-card {
  border-radius: 8px;
}

.filter-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid #edf2f7;
  margin-bottom: 10px;
}

.panel-copy {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.panel-title {
  color: #1f2329;
  font-size: 15px;
  font-weight: 700;
}

.dimension-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dimension-toggle {
  flex-shrink: 0;
}

.dimension-select {
  width: 220px;
}

.scope-actions {
  margin-left: auto;
}

.filter-form {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px 18px;
  align-items: start;
}

.stats-filter-form {
  align-items: stretch;
}

.stats-filter-form :deep(.el-form-item) {
  width: 100%;
  margin: 0;
}

.stats-filter-form :deep(.el-form-item__content) {
  width: 100%;
  min-width: 0;
}

.stats-filter-form :deep(.el-select),
.stats-filter-form :deep(.el-input) {
  width: 100% !important;
}

.stats-filter-form .scope-actions {
  grid-column: 3 / -1;
  justify-self: end;
  display: flex;
  align-items: center;
  gap: 12px;
  margin-left: 0;
}

.metric-card {
  text-align: center;
}

.metric-label {
  color: #606266;
  font-size: 14px;
}

.metric-value {
  margin-top: 12px;
  color: #1f2329;
  font-size: 28px;
  font-weight: 700;
}

.chart-card {
  min-height: 360px;
}

.chart-title {
  margin-bottom: 12px;
  font-size: 17px;
  font-weight: 600;
}

.chart-hint {
  margin-bottom: 12px;
  color: #8a94a6;
  font-size: 12px;
  line-height: 1.5;
}

.chart-box {
  width: 100%;
  height: 300px;
}

.table-card {
  min-height: 200px;
}

.table-header {
  font-size: 17px;
  font-weight: 600;
}

.pager-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

@media (max-width: 960px) {
  .filter-head {
    flex-direction: column;
    align-items: stretch;
  }

  .dimension-control {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .dimension-select {
    width: 100%;
  }

  .scope-actions {
    width: 100%;
    grid-column: 1 / -1;
    justify-self: stretch;
    justify-content: flex-start;
  }
}
</style>
