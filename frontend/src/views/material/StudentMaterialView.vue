<template>
  <div class="material-page">
    <el-card>
      <template #header>
        <span class="title">我的实习材料</span>
      </template>

      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model.trim="query.keyword" clearable placeholder="计划/材料类型" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.materialStatus" clearable placeholder="全部">
            <el-option label="未提交" value="NOT_SUBMITTED" />
            <el-option label="已提交" value="SUBMITTED" />
            <el-option label="已逾期" value="OVERDUE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchData">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="records" border>
        <el-table-column prop="planCode" label="计划编码" width="150" />
        <el-table-column prop="planName" label="计划名称" min-width="180" />
        <el-table-column prop="materialTypeName" label="材料类型" width="160" />
        <el-table-column prop="deadlineTime" label="截止时间" width="170" />
        <el-table-column prop="latestVersionNo" label="当前版本" width="100" />
        <el-table-column prop="materialStatus" label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.materialStatus)">{{ statusLabel(row.materialStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="补交状态" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.lateSubmitOpen" type="warning">补交开放中</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="lastSubmitTime" label="最近提交时间" width="170" />
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'material:upload'"
              link
              type="primary"
              :disabled="!row.canSubmit"
              @click="openUploadDialog(row)"
            >
              上传
            </el-button>
            <el-button link type="info" @click="openVersionDialog(row)">版本历史</el-button>
            <el-button
              link
              type="warning"
              :disabled="!row.latestVersion || !row.latestVersion.previewable"
              @click="previewLatest(row)"
            >
              预览
            </el-button>
            <el-button link type="success" :disabled="!row.latestVersion" @click="downloadLatest(row)">下载</el-button>
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

    <material-upload-dialog v-model="uploadVisible" :material="currentMaterial" @success="handleUploadSuccess" />

    <material-version-history-dialog
      v-model="versionVisible"
      :material-id="currentMaterial?.materialId"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  fetchMaterialDownloadBlobApi,
  fetchMaterialPreviewBlobApi,
  queryStudentMaterialPageApi
} from '@/api/modules/material';
import type { MaterialListItem } from '@/types/api';
import MaterialUploadDialog from './components/MaterialUploadDialog.vue';
import MaterialVersionHistoryDialog from './components/MaterialVersionHistoryDialog.vue';
import { openMaterialVersionPreview } from '@/utils/material-preview';

const loading = ref(false);
const records = ref<MaterialListItem[]>([]);
const total = ref(0);

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  materialStatus: ''
});

const uploadVisible = ref(false);
const versionVisible = ref(false);
const currentMaterial = ref<MaterialListItem | null>(null);

async function fetchData() {
  loading.value = true;
  try {
    const resp = await queryStudentMaterialPageApi({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      materialStatus: query.materialStatus || undefined
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
  query.materialStatus = '';
  fetchData();
}

function openUploadDialog(row: MaterialListItem) {
  if (!row.canSubmit) {
    ElMessage.warning('当前材料不可提交，请检查截止时间或补交状态');
    return;
  }
  currentMaterial.value = row;
  uploadVisible.value = true;
}

function openVersionDialog(row: MaterialListItem) {
  currentMaterial.value = row;
  versionVisible.value = true;
}

function statusLabel(status: string) {
  if (status === 'SUBMITTED') {
    return '已提交';
  }
  if (status === 'OVERDUE') {
    return '已逾期';
  }
  return '未提交';
}

function statusTagType(status: string): 'success' | 'warning' | 'info' {
  if (status === 'SUBMITTED') {
    return 'success';
  }
  if (status === 'OVERDUE') {
    return 'warning';
  }
  return 'info';
}

async function previewLatest(row: MaterialListItem) {
  if (!row.latestVersion || !row.latestVersion.previewable) {
    ElMessage.warning('当前版本不支持在线预览');
    return;
  }
  await openMaterialVersionPreview(row.latestVersion, () => fetchMaterialPreviewBlobApi(row.latestVersion!.id));
}

async function downloadLatest(row: MaterialListItem) {
  if (!row.latestVersion) {
    ElMessage.warning('暂无可下载版本');
    return;
  }
  const blob = await fetchMaterialDownloadBlobApi(row.latestVersion.id);
  const objectUrl = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = objectUrl;
  a.download = row.latestVersion.fileName || `材料_v${row.latestVersion.versionNo}`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(objectUrl);
}

function handleUploadSuccess() {
  fetchData();
}

onMounted(() => {
  fetchData();
});
</script>

<style scoped lang="scss">
.material-page {
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
