<template>
  <div class="evaluation-student-page">
    <el-card>
      <template #header>
        <span class="title">我的评价结果</span>
      </template>

      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model.trim="query.keyword" clearable placeholder="计划/材料/评价人" />
        </el-form-item>
        <el-form-item label="评价类型">
          <el-select v-model="query.evaluationType" clearable placeholder="全部">
            <el-option label="过程评价" value="PROCESS" />
            <el-option label="综合评价" value="FINAL" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="records" border>
        <el-table-column prop="planCode" label="计划编码" width="150" />
        <el-table-column prop="planName" label="计划名称" min-width="170" />
        <el-table-column prop="evaluationType" label="类型" width="110">
          <template #default="{ row }">
            <el-tag :type="row.evaluationType === 'FINAL' ? 'success' : 'info'">
              {{ row.evaluationType === 'FINAL' ? '综合评价' : '过程评价' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="materialTypeName" label="材料类型" width="150" />
        <el-table-column prop="materialVersionNo" label="版本" width="90">
          <template #default="{ row }">{{ row.materialVersionNo ? `第${row.materialVersionNo}版` : '-' }}</template>
        </el-table-column>
        <el-table-column prop="evaluatorRole" label="评价角色" width="120" />
        <el-table-column prop="evaluatorName" label="评价教师" width="120" />
        <el-table-column prop="score" label="评分" width="90" />
        <el-table-column prop="commentText" label="评语" min-width="220" />
        <el-table-column prop="evaluatedTime" label="评价时间" width="170" />
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { queryStudentEvaluationPageApi } from '@/api/modules/evaluation';
import type { StudentEvaluationItem } from '@/types/api';

const loading = ref(false);
const records = ref<StudentEvaluationItem[]>([]);
const total = ref(0);

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  evaluationType: '' as 'PROCESS' | 'FINAL' | ''
});

async function fetchData() {
  loading.value = true;
  try {
    const resp = await queryStudentEvaluationPageApi({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      evaluationType: query.evaluationType || undefined
    });
    records.value = resp.data.records || [];
    total.value = Number(resp.data.total || 0);
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
  query.evaluationType = '';
  fetchData();
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.evaluation-student-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
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
