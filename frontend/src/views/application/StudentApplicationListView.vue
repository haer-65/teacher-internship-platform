<template>
  <div class="page-wrap">
    <el-card>
      <template #header>
        <div class="header-row">
          <span class="title">可申请的实习计划</span>
        </div>
      </template>

      <el-empty
        v-if="!availablePlans.length"
        description="当前没有可申请的实习计划"
      />
      <div v-else class="plan-grid">
        <div v-for="plan in availablePlans" :key="String(plan.id)" class="plan-card">
          <div class="plan-card__header">
            <div>
              <div class="plan-card__title">{{ plan.planName }}</div>
              <div class="plan-card__meta">{{ plan.planCode }} / {{ plan.academicYear }} / {{ plan.term }}</div>
            </div>
            <el-tag type="success">可申请</el-tag>
          </div>
          <div class="plan-card__deadline">申请截止：{{ plan.applyDeadline }}</div>
          <div class="plan-card__bases">
            <span v-for="base in plan.planBases.slice(0, 3)" :key="String(base.baseId)" class="base-pill">
              {{ base.baseName }}
            </span>
            <span v-if="plan.planBases.length > 3" class="base-pill base-pill--muted">+{{ plan.planBases.length - 3 }}</span>
          </div>
          <div class="plan-card__actions">
            <el-button plain @click="goPlanDetail(plan.id)">计划详情</el-button>
            <el-button type="primary" @click="handleCreate(plan)">填写申请</el-button>
          </div>
        </div>
      </div>
    </el-card>

    <el-card>
      <template #header>
        <div class="header-row">
          <span class="title">我的申请</span>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="关键词">
          <el-input
            v-model.trim="queryForm.keyword"
            placeholder="计划名称/个人陈述"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="申请状态">
          <el-select v-model="queryForm.applicationStatus" clearable placeholder="全部">
            <el-option label="待审核" value="SUBMITTED" />
            <el-option label="已通过" value="APPROVED" />
            <el-option label="已拒绝" value="REJECTED" />
            <el-option label="已撤回" value="WITHDRAWN" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border>
        <el-table-column prop="planCode" label="计划编号" width="150" />
        <el-table-column prop="planName" label="计划名称" min-width="180" />
        <el-table-column label="实习基地志愿" min-width="220">
          <template #default="{ row }">{{ formatPreferredBases(row.preferredBases) }}</template>
        </el-table-column>
        <el-table-column prop="personalStatement" label="个人陈述" min-width="220" show-overflow-tooltip />
        <el-table-column prop="applicationStatus" label="申请状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.applicationStatus)">{{ statusLabel(row.applicationStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="submittedTime" label="提交时间" width="170" />
        <el-table-column prop="reviewedByName" label="审核人" width="130" />
        <el-table-column prop="reviewedTime" label="审核时间" width="170" />
        <el-table-column prop="reviewComment" label="审核意见" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="action-wrap">
              <el-button link type="primary" :disabled="!row.canEdit" @click="goEdit(row.id)">编辑</el-button>
              <el-button link type="danger" :disabled="!row.canWithdraw" @click="handleWithdraw(row.id)">撤回</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :current-page="page"
          :page-size="size"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { queryStudentApplicationPageApi, queryStudentAvailablePlansApi, withdrawStudentApplicationApi } from '@/api/modules/application';
import type { ApplicationPreferenceItem, ApplicationStatus, IdValue, StudentApplicationItem, StudentAvailablePlanItem } from '@/types/api';

const router = useRouter();

const loading = ref(false);
const tableData = ref<StudentApplicationItem[]>([]);
const availablePlans = ref<StudentAvailablePlanItem[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);

const queryForm = reactive({
  keyword: '',
  applicationStatus: '' as ApplicationStatus | ''
});

function statusTagType(status: ApplicationStatus): '' | 'success' | 'warning' | 'info' | 'danger' {
  switch (status) {
    case 'SUBMITTED':
      return 'warning';
    case 'APPROVED':
      return 'success';
    case 'REJECTED':
      return 'danger';
    case 'WITHDRAWN':
      return 'info';
    default:
      return '';
  }
}

function statusLabel(status: ApplicationStatus): string {
  switch (status) {
    case 'SUBMITTED':
      return '待审核';
    case 'APPROVED':
      return '已通过';
    case 'REJECTED':
      return '已拒绝';
    case 'WITHDRAWN':
      return '已撤回';
    default:
      return status;
  }
}

function formatPreferredBases(preferredBases: ApplicationPreferenceItem[] = []) {
  if (!preferredBases.length) {
    return '-';
  }
  return preferredBases
    .map((item) => `第${item.preferenceOrder}志愿：${item.baseName || item.baseCode || '-'}`)
    .join('，');
}

function hasSubmittedApplication(plan: StudentAvailablePlanItem) {
  return Boolean(plan.applicationId && plan.applicationStatus && plan.applicationStatus !== 'WITHDRAWN');
}

async function fetchAvailablePlans() {
  const resp = await queryStudentAvailablePlansApi();
  availablePlans.value = resp.data || [];
}

async function fetchData() {
  loading.value = true;
  try {
    const resp = await queryStudentApplicationPageApi({
      page: page.value,
      size: size.value,
      keyword: queryForm.keyword || undefined,
      applicationStatus: queryForm.applicationStatus || undefined
    });
    tableData.value = resp.data.records || [];
    total.value = Number(resp.data.total || 0);
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  page.value = 1;
  fetchData();
}

function handleReset() {
  queryForm.keyword = '';
  queryForm.applicationStatus = '';
  page.value = 1;
  fetchData();
}

function handleSizeChange(nextSize: number) {
  size.value = nextSize;
  page.value = 1;
  fetchData();
}

function handleCurrentChange(nextPage: number) {
  page.value = nextPage;
  fetchData();
}

function goCreate(planId: IdValue) {
  router.push(`/application/student/form?planId=${planId}`);
}

function handleCreate(plan: StudentAvailablePlanItem) {
  if (hasSubmittedApplication(plan)) {
    ElMessage.warning('你已经提交过该计划的申请，请到“我的申请”中查看或撤回后再重新申请。');
    return;
  }
  goCreate(plan.id);
}

function goPlanDetail(planId: IdValue) {
  router.push(`/plan/student/detail/${planId}`);
}

function goEdit(applicationId: IdValue) {
  router.push(`/application/student/form/${applicationId}`);
}

async function handleWithdraw(applicationId: IdValue) {
  await ElMessageBox.confirm('确认撤回这条申请吗？', '提示', { type: 'warning' });
  await withdrawStudentApplicationApi(applicationId);
  ElMessage.success('申请已撤回');
  await Promise.all([fetchData(), fetchAvailablePlans()]);
}

onMounted(async () => {
  await Promise.all([fetchAvailablePlans(), fetchData()]);
});
</script>

<style scoped lang="scss">
.page-wrap {
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

.plan-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.plan-card {
  border: 1px solid #e8ecff;
  border-radius: 16px;
  padding: 18px;
  background: linear-gradient(180deg, rgba(248, 250, 255, 0.96), rgba(255, 255, 255, 0.98));
  box-shadow: 0 10px 28px rgba(31, 61, 115, 0.06);
}

.plan-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.plan-card__title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.plan-card__meta {
  margin-top: 4px;
  color: #64748b;
  font-size: 13px;
}

.plan-card__deadline {
  margin-top: 12px;
  color: #475569;
  font-size: 14px;
}

.plan-card__bases {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 14px;
}

.base-pill {
  display: inline-flex;
  align-items: center;
  padding: 6px 10px;
  border-radius: 999px;
  background: rgba(64, 158, 255, 0.1);
  color: #2d6cdf;
  font-size: 12px;
}

.base-pill--muted {
  background: rgba(148, 163, 184, 0.12);
  color: #64748b;
}

.plan-card__actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 18px;
}

.query-form {
  margin-bottom: 12px;
}

.action-wrap {
  display: flex;
  gap: 8px;
}

.pager-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
