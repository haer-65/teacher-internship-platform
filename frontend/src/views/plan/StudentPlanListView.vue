<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span class="title">已发布实习计划</span>
      </div>
    </template>

    <el-form :inline="true" :model="queryForm" class="query-form">
      <el-form-item label="关键词">
        <el-input
          v-model.trim="queryForm.keyword"
          clearable
          placeholder="计划编码/计划名称/学年/学期"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table v-loading="loading" :data="tableData" border>
      <el-table-column prop="planCode" label="计划编码" width="140" />
      <el-table-column prop="planName" label="计划名称" min-width="180" />
      <el-table-column prop="academicYear" label="学年" width="140" />
      <el-table-column prop="term" label="学期" width="120" />
      <el-table-column prop="remainingQuota" label="剩余名额" width="100">
        <template #default="{ row }">
          {{ row.remainingQuota ?? row.studentQuota }}
        </template>
      </el-table-column>
      <el-table-column prop="applyDeadline" label="申请截止时间" width="170" />
      <el-table-column prop="planStatus" label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="planStatusTagType(row.planStatus, row.applyDeadline, row.remainingQuota)">
            {{ planStatusLabel(row.planStatus, row.applyDeadline, row.remainingQuota) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startTime" label="开始时间" width="170" />
      <el-table-column prop="endTime" label="结束时间" width="170" />
      <el-table-column label="操作" width="100" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="goDetail(row.id)">详情</el-button>
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
import { queryStudentPlanPageApi } from '@/api/modules/plan';
import type { IdValue, PlanListItem } from '@/types/api';

const router = useRouter();

const loading = ref(false);
const tableData = ref<PlanListItem[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);

const queryForm = reactive({
  keyword: ''
});

function isApplyOpen(applyDeadline?: string) {
  if (!applyDeadline) {
    return false;
  }
  return new Date(applyDeadline).getTime() >= Date.now();
}

function isPlanFull(remainingQuota?: number) {
  return typeof remainingQuota === 'number' && remainingQuota <= 0;
}

function planStatusTagType(planStatus?: string, applyDeadline?: string, remainingQuota?: number): '' | 'success' | 'warning' | 'info' | 'danger' {
  if (planStatus === 'PUBLISHED') {
    if (!isApplyOpen(applyDeadline)) {
      return 'warning';
    }
    return isPlanFull(remainingQuota) ? 'danger' : 'success';
  }
  return 'info';
}

function planStatusLabel(planStatus?: string, applyDeadline?: string, remainingQuota?: number) {
  if (planStatus === 'PUBLISHED') {
    if (!isApplyOpen(applyDeadline)) {
      return '报名截止';
    }
    return isPlanFull(remainingQuota) ? '名额已满' : '可申请';
  }
  if (planStatus === 'FINISHED') {
    return '已结束';
  }
  if (planStatus === 'ARCHIVED') {
    return '已归档';
  }
  return planStatus || '-';
}

async function fetchData() {
  loading.value = true;
  try {
    const resp = await queryStudentPlanPageApi({
      page: page.value,
      size: size.value,
      keyword: queryForm.keyword || undefined
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

function goDetail(id: IdValue) {
  router.push(`/plan/student/detail/${id}`);
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

.pager-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
