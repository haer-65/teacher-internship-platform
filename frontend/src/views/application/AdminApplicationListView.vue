<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span class="title">申请审核</span>
        <el-button type="primary" @click="goAssignment()">前往分配管理</el-button>
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
          <el-option label="已驳回" value="REJECTED" />
          <el-option label="已撤回" value="WITHDRAWN" />
        </el-select>
      </el-form-item>
      <el-form-item label="学号">
        <el-input v-model.trim="queryForm.studentNo" clearable />
      </el-form-item>
      <el-form-item label="姓名">
        <el-input v-model.trim="queryForm.studentName" clearable />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="planCode" label="计划编号" width="150" />
      <el-table-column prop="planName" label="计划名称" min-width="200" />
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="studentName" label="姓名" width="120" />
      <el-table-column label="院系/专业/年级" min-width="220">
        <template #default="{ row }">{{ formatStudentOrgInfo(row) }}</template>
      </el-table-column>
      <el-table-column label="实习基地志愿" min-width="240">
        <template #default="{ row }">{{ formatPreferredBases(row.preferredBases) }}</template>
      </el-table-column>
      <el-table-column prop="personalStatement" label="个人陈述" min-width="220" show-overflow-tooltip />
      <el-table-column prop="applicationStatus" label="申请状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.applicationStatus)">{{ statusLabel(row.applicationStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="submittedTime" label="提交时间" width="170" />
      <el-table-column prop="reviewedByName" label="审核人" width="120" />
      <el-table-column prop="reviewedTime" label="审核时间" width="170" />
      <el-table-column prop="reviewComment" label="审核意见" min-width="180" show-overflow-tooltip />
      <el-table-column label="分配状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.currentAssignmentId ? 'success' : 'info'">
            {{ row.currentAssignmentId ? '已分配' : '未分配' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <div class="action-wrap">
            <el-button
              v-permission="'application:assign'"
              link
              type="success"
              :disabled="row.applicationStatus !== 'SUBMITTED'"
              @click="handleApprove(row.id)"
            >
              通过
            </el-button>
            <el-button
              v-permission="'application:assign'"
              link
              type="danger"
              :disabled="row.applicationStatus !== 'SUBMITTED'"
              @click="handleReject(row.id)"
            >
              驳回
            </el-button>
            <el-button
              v-permission="'application:assign'"
              link
              type="primary"
              :disabled="row.applicationStatus !== 'APPROVED' || row.planStatus !== 'PUBLISHED'"
              @click="handleAssignmentEntry(row)"
            >
              {{ row.currentAssignmentId ? '调整分配' : '去分配' }}
            </el-button>
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
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { queryAdminApplicationPageApi, reviewApplicationApi } from '@/api/modules/application';
import type { AdminApplicationItem, ApplicationPreferenceItem, ApplicationStatus, IdValue } from '@/types/api';

const router = useRouter();

const loading = ref(false);
const tableData = ref<AdminApplicationItem[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);

const queryForm = reactive({
  keyword: '',
  applicationStatus: '' as ApplicationStatus | '',
  studentNo: '',
  studentName: ''
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
      return '已驳回';
    case 'WITHDRAWN':
      return '已撤回';
    default:
      return status;
  }
}

function formatStudentOrgInfo(row: AdminApplicationItem) {
  const parts = [row.studentDeptName, row.studentMajorName, row.studentGradeName].filter(Boolean);
  return parts.length ? parts.join(' / ') : '-';
}

function formatPreferredBases(preferredBases: ApplicationPreferenceItem[] = []) {
  if (!preferredBases.length) {
    return '-';
  }
  return preferredBases
    .map((item) => `第${item.preferenceOrder}志愿：${item.baseName || item.baseCode || '-'}`)
    .join('；');
}

async function fetchData() {
  loading.value = true;
  try {
    const resp = await queryAdminApplicationPageApi({
      page: page.value,
      size: size.value,
      keyword: queryForm.keyword || undefined,
      applicationStatus: queryForm.applicationStatus || undefined,
      studentNo: queryForm.studentNo || undefined,
      studentName: queryForm.studentName || undefined
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
  queryForm.studentNo = '';
  queryForm.studentName = '';
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

async function handleApprove(applicationId: IdValue) {
  await ElMessageBox.confirm('确认通过这条申请吗？', '提示', { type: 'warning' });
  await reviewApplicationApi(applicationId, { status: 'APPROVED' });
  ElMessage.success('申请已通过');
  await fetchData();
}

async function handleReject(applicationId: IdValue) {
  const promptResult = await ElMessageBox.prompt('请输入驳回原因', '驳回申请', {
    inputType: 'textarea',
    inputPlaceholder: '请填写驳回原因',
    confirmButtonText: '确定',
    cancelButtonText: '取消'
  });
  await reviewApplicationApi(applicationId, {
    status: 'REJECTED',
    reviewComment: promptResult.value
  });
  ElMessage.success('申请已驳回');
  await fetchData();
}

function normalizeRouteIdValue(value?: IdValue) {
  if (value === undefined || value === null || value === '') {
    return undefined;
  }
  return String(value);
}

function goAssignment(planId?: IdValue, applicationId?: IdValue, assignmentId?: IdValue) {
  const search = new URLSearchParams();
  const normalizedPlanId = normalizeRouteIdValue(planId);
  const normalizedApplicationId = normalizeRouteIdValue(applicationId);
  const normalizedAssignmentId = normalizeRouteIdValue(assignmentId);

  if (normalizedPlanId !== undefined) {
    search.set('planId', normalizedPlanId);
  }
  if (normalizedApplicationId !== undefined) {
    search.set('applicationId', normalizedApplicationId);
  }
  if (normalizedAssignmentId !== undefined) {
    search.set('assignmentId', normalizedAssignmentId);
  }

  const suffix = search.toString();
  router.push(suffix ? `/application/assignment?${suffix}` : '/application/assignment');
}

function handleAssignmentEntry(row: AdminApplicationItem) {
  if (row.currentAssignmentId) {
    goAssignment(row.planId, row.id, row.currentAssignmentId);
    return;
  }
  goAssignment(row.planId, row.id);
}

onMounted(() => {
  fetchData();
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
