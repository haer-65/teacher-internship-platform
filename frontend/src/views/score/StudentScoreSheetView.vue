<template>
  <div class="student-score-page">
    <el-card>
      <template #header>
        <span class="title">我的成绩单</span>
      </template>

      <el-form :inline="true" :model="query" class="query-form">
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
        <el-form-item label="关键词">
          <el-input v-model.trim="query.keyword" clearable placeholder="计划关键词" style="width: 220px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="records" border>
        <el-table-column prop="planCode" label="计划编码" width="130" />
        <el-table-column prop="planName" label="计划名称" min-width="170" />
        <el-table-column prop="processScore" label="过程分" width="90" />
        <el-table-column prop="finalScore" label="综合分" width="90" />
        <el-table-column prop="totalScore" label="总分" width="90">
          <template #default="{ row }">
            <el-tag type="success">{{ showScore(row.totalScore) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'">
              {{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="publishedTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="130" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.id)">查看成绩单</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { queryStudentScorePageApi, queryScorePlanOptionsApi } from '@/api/modules/score';
import type { IdValue, ScoreListItem } from '@/types/api';
import { normalizeRouteId } from '@/utils/id';

const router = useRouter();

const loading = ref(false);
const records = ref<ScoreListItem[]>([]);
const total = ref(0);
const planOptions = ref<Array<{ id: IdValue; name: string }>>([]);

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  planId: '' as string
});

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
    const resp = await queryStudentScorePageApi({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      planId: planId || undefined
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
  query.planId = '';
  fetchData();
}

function goDetail(scoreSheetId: IdValue) {
  router.push(`/score/detail/${scoreSheetId}`);
}

function showScore(value?: number): string {
  if (value === undefined || value === null || Number.isNaN(value)) {
    return '0.00';
  }
  return Number(value).toFixed(2);
}

onMounted(() => {
  loadPlanOptions();
  fetchData();
});
</script>

<style scoped lang="scss">
.student-score-page {
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
