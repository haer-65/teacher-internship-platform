<template>
  <div class="evaluation-page">
    <el-card>
      <template #header>
        <span class="title">教师待评价列表</span>
      </template>

      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="过程评价待办" name="process">
          <el-form :inline="true" :model="processQuery" class="query-form">
            <el-form-item label="关键词">
              <el-input v-model.trim="processQuery.keyword" clearable placeholder="计划/学生/材料" />
            </el-form-item>
            <el-form-item label="学号">
              <el-input v-model.trim="processQuery.studentNo" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchProcessData">查询</el-button>
              <el-button @click="resetProcessQuery">重置</el-button>
            </el-form-item>
            <el-form-item>
              <el-switch
                v-model="processPendingOnly"
                active-text="仅看待办"
                inactive-text="全部"
                @change="fetchProcessData"
              />
            </el-form-item>
          </el-form>

          <el-table v-loading="processLoading" :data="processRecords" border>
            <el-table-column prop="planCode" label="计划编码" width="150" />
            <el-table-column prop="planName" label="计划名称" min-width="170" />
            <el-table-column prop="studentNo" label="学号" width="120" />
            <el-table-column prop="studentName" label="学生" width="120" />
            <el-table-column prop="materialTypeName" label="材料类型" width="150" />
            <el-table-column prop="materialVersionNo" label="版本" width="90" />
            <el-table-column prop="submittedTime" label="提交时间" width="170" />
            <el-table-column prop="myEvaluationCount" label="我的评价次数" width="120" />
            <el-table-column label="待办" width="90">
              <template #default="{ row }">
                <el-tag :type="row.pending ? 'warning' : 'success'">{{ row.pending ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" @click="goProcessDetail(row.materialVersionId)">去评价</el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager-wrap">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="processTotal"
              :current-page="processQuery.page"
              :page-size="processQuery.size"
              :page-sizes="[10, 20, 50, 100]"
              @size-change="handleProcessSizeChange"
              @current-change="handleProcessCurrentChange"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="综合评价待办" name="final">
          <el-form :inline="true" :model="finalQuery" class="query-form">
            <el-form-item label="关键词">
              <el-input v-model.trim="finalQuery.keyword" clearable placeholder="计划/学生" />
            </el-form-item>
            <el-form-item label="学号">
              <el-input v-model.trim="finalQuery.studentNo" clearable />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="fetchFinalData">查询</el-button>
              <el-button @click="resetFinalQuery">重置</el-button>
            </el-form-item>
            <el-form-item>
              <el-switch
                v-model="finalPendingOnly"
                active-text="仅看待办"
                inactive-text="全部"
                @change="fetchFinalData"
              />
            </el-form-item>
          </el-form>

          <el-table v-loading="finalLoading" :data="finalRecords" border>
            <el-table-column prop="planCode" label="计划编码" width="150" />
            <el-table-column prop="planName" label="计划名称" min-width="170" />
            <el-table-column prop="planStatus" label="计划状态" width="120" />
            <el-table-column prop="studentNo" label="学号" width="120" />
            <el-table-column prop="studentName" label="学生" width="120" />
            <el-table-column prop="myEvaluationCount" label="我的综合评价次数" width="140" />
            <el-table-column label="可评价" width="90">
              <template #default="{ row }">
                <el-tag :type="row.canEvaluate ? 'success' : 'info'">{{ row.canEvaluate ? '是' : '否' }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="140" fixed="right">
              <template #default="{ row }">
                <el-button link type="primary" :disabled="!row.canEvaluate" @click="goFinalPage(row.assignmentId)">
                  去综合评价
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <div class="pager-wrap">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="finalTotal"
              :current-page="finalQuery.page"
              :page-size="finalQuery.size"
              :page-sizes="[10, 20, 50, 100]"
              @size-change="handleFinalSizeChange"
              @current-change="handleFinalCurrentChange"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import {
  queryTeacherFinalPendingPageApi,
  queryTeacherProcessPendingPageApi
} from '@/api/modules/evaluation';
import type { TeacherFinalPendingItem, TeacherProcessPendingItem } from '@/types/api';

const router = useRouter();
const activeTab = ref('process');

const processLoading = ref(false);
const processRecords = ref<TeacherProcessPendingItem[]>([]);
const processTotal = ref(0);
const processPendingOnly = ref(true);
const processQuery = reactive({
  page: 1,
  size: 10,
  keyword: '',
  studentNo: ''
});

const finalLoading = ref(false);
const finalRecords = ref<TeacherFinalPendingItem[]>([]);
const finalTotal = ref(0);
const finalPendingOnly = ref(true);
const finalQuery = reactive({
  page: 1,
  size: 10,
  keyword: '',
  studentNo: ''
});

async function fetchProcessData() {
  processLoading.value = true;
  try {
    const resp = await queryTeacherProcessPendingPageApi({
      page: processQuery.page,
      size: processQuery.size,
      keyword: processQuery.keyword || undefined,
      studentNo: processQuery.studentNo || undefined,
      pendingOnly: processPendingOnly.value ? 1 : undefined
    });
    processRecords.value = resp.data.records || [];
    processTotal.value = Number(resp.data.total || 0);
  } finally {
    processLoading.value = false;
  }
}

async function fetchFinalData() {
  finalLoading.value = true;
  try {
    const resp = await queryTeacherFinalPendingPageApi({
      page: finalQuery.page,
      size: finalQuery.size,
      keyword: finalQuery.keyword || undefined,
      studentNo: finalQuery.studentNo || undefined,
      pendingOnly: finalPendingOnly.value ? 1 : undefined
    });
    finalRecords.value = resp.data.records || [];
    finalTotal.value = Number(resp.data.total || 0);
  } finally {
    finalLoading.value = false;
  }
}

function handleTabChange() {
  if (activeTab.value === 'process') {
    fetchProcessData();
    return;
  }
  fetchFinalData();
}

function handleProcessSizeChange(size: number) {
  processQuery.size = size;
  processQuery.page = 1;
  fetchProcessData();
}

function handleProcessCurrentChange(page: number) {
  processQuery.page = page;
  fetchProcessData();
}

function resetProcessQuery() {
  processQuery.page = 1;
  processQuery.size = 10;
  processQuery.keyword = '';
  processQuery.studentNo = '';
  processPendingOnly.value = true;
  fetchProcessData();
}

function handleFinalSizeChange(size: number) {
  finalQuery.size = size;
  finalQuery.page = 1;
  fetchFinalData();
}

function handleFinalCurrentChange(page: number) {
  finalQuery.page = page;
  fetchFinalData();
}

function resetFinalQuery() {
  finalQuery.page = 1;
  finalQuery.size = 10;
  finalQuery.keyword = '';
  finalQuery.studentNo = '';
  finalPendingOnly.value = true;
  fetchFinalData();
}

function goProcessDetail(materialVersionId: string | number) {
  router.push(`/evaluation/teacher/process/detail/${String(materialVersionId)}`);
}

function goFinalPage(assignmentId: number) {
  router.push({
    path: '/evaluation/teacher/final',
    query: { assignmentId: String(assignmentId) }
  });
}

onMounted(() => {
  fetchProcessData();
});
</script>

<style scoped lang="scss">
.evaluation-page {
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
