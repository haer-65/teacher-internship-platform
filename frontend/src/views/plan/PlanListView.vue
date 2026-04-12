<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <div class="title">实习计划管理</div>
        <el-button v-permission="'plan:publish'" type="primary" @click="goCreate">新建草稿</el-button>
      </div>
    </template>

    <el-form :inline="true" :model="queryForm" class="query-form">
      <el-form-item label="关键词">
        <el-input v-model.trim="queryForm.keyword" clearable placeholder="计划编码/计划名称/学年/学期" @keyup.enter="handleSearch" />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" clearable placeholder="全部">
          <el-option label="草稿" value="DRAFT" />
          <el-option label="已发布" value="PUBLISHED" />
          <el-option label="已结束" value="FINISHED" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="planCode" label="计划编码" width="150" />
      <el-table-column prop="planName" label="计划名称" min-width="220" />
      <el-table-column prop="academicYear" label="学年" width="130" />
      <el-table-column prop="term" label="学期" width="110" />
      <el-table-column prop="studentQuota" label="名额" width="90" />
      <el-table-column prop="planStatus" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.planStatus)">{{ statusLabel(row.planStatus) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column prop="applyDeadline" label="报名截止时间" width="180" />
      <el-table-column label="操作" width="380" fixed="right">
        <template #default="{ row }">
          <div class="action-wrap">
            <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
            <el-button v-permission="'plan:publish'" link type="primary" :disabled="row.planStatus !== 'DRAFT'" @click="goEdit(row.id)">编辑</el-button>
            <el-button v-permission="'plan:publish'" link type="warning" :disabled="row.planStatus !== 'DRAFT'" @click="handlePublish(row.id)">发布</el-button>
            <el-button
              v-permission="'plan:publish'"
              link
              type="danger"
              :disabled="row.planStatus !== 'DRAFT' && row.planStatus !== 'ARCHIVED'"
              @click="handleDelete(row.id, row.planStatus)"
            >
              删除
            </el-button>
            <el-button v-permission="'plan:publish'" link type="warning" :disabled="row.planStatus !== 'PUBLISHED'" @click="handleFinish(row.id)">结束</el-button>
            <el-button v-permission="'plan:publish'" link type="danger" :disabled="row.planStatus !== 'FINISHED'" @click="handleArchive(row.id)">归档</el-button>
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
import { archivePlanApi, deletePlanApi, finishPlanApi, publishPlanApi, queryPlanPageApi } from '@/api/modules/plan';
import type { IdValue, PlanListItem, PlanStatus } from '@/types/api';
import { useAuthStore } from '@/store/modules/auth';

const router = useRouter();
const authStore = useAuthStore();

const loading = ref(false);
const tableData = ref<PlanListItem[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);

const queryForm = reactive({
  keyword: '',
  status: '' as PlanStatus | ''
});

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

async function fetchData() {
  loading.value = true;
  try {
    const resp = await queryPlanPageApi({
      page: page.value,
      size: size.value,
      keyword: queryForm.keyword || undefined,
      status: queryForm.status || undefined
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
  queryForm.status = '';
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

function goCreate() {
  router.push('/plan/edit');
}

function goEdit(id: IdValue) {
  router.push(`/plan/edit/${id}`);
}

function goDetail(id: IdValue) {
  router.push(`/plan/detail/${id}`);
}

async function handlePublish(id: IdValue) {
  await ElMessageBox.confirm('确认发布该草稿计划吗？', '提示', { type: 'warning' });
  await publishPlanApi(id);
  ElMessage.success('计划发布成功');
  fetchData();
}

async function handleDelete(id: IdValue, status: PlanStatus) {
  const targetLabel = status === 'ARCHIVED' ? '已归档计划' : '草稿计划';
  await ElMessageBox.confirm(`确认删除该${targetLabel}吗？删除后将同时清理该计划的基地配置、材料配置和附件记录。`, '提示', {
    type: 'warning'
  });
  await deletePlanApi(id);
  ElMessage.success('计划删除成功');
  fetchData();
}

async function handleFinish(id: IdValue) {
  await ElMessageBox.confirm('确认结束该已发布计划吗？', '提示', { type: 'warning' });
  await finishPlanApi(id);
  ElMessage.success('计划结束成功');
  fetchData();
}

async function handleArchive(id: IdValue) {
  await ElMessageBox.confirm('确认归档该已结束计划吗？', '提示', { type: 'warning' });
  await archivePlanApi(id);
  ElMessage.success('计划归档成功');
  fetchData();
}

onMounted(() => {
  if (authStore.currentRoleCode === 'STUDENT') {
    router.replace('/plan/student');
    return;
  }
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
  font-size: 20px;
  font-weight: 600;
}

.query-form {
  margin-bottom: 16px;
}

.action-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.pager-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
