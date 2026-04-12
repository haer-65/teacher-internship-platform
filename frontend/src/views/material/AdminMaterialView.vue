<template>
  <div class="material-page">
    <el-card>
      <template #header>
        <span class="title">材料管理（管理员）</span>
      </template>

      <el-form :inline="true" :model="query" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model.trim="query.keyword" clearable placeholder="计划/学生/材料" />
        </el-form-item>
        <el-form-item label="学号">
          <el-input v-model.trim="query.studentNo" clearable />
        </el-form-item>
        <el-form-item label="学生姓名">
          <el-input v-model.trim="query.studentName" clearable />
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
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="学生" width="120" />
        <el-table-column prop="materialTypeName" label="材料类型" width="160" />
        <el-table-column prop="deadlineTime" label="截止时间" width="170" />
        <el-table-column prop="latestVersionNo" label="版本" width="90" />
        <el-table-column prop="materialStatus" label="状态" width="110">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.materialStatus)">{{ statusLabel(row.materialStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="补交" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.lateSubmitOpen" type="warning">开放</el-tag>
            <span v-else>关闭</span>
          </template>
        </el-table-column>
        <el-table-column prop="lateSubmitUntil" label="补交截止" width="170" />
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="{ row }">
            <el-button link type="info" @click="openVersionDialog(row)">版本历史</el-button>
            <el-button
              v-permission="'material:manage'"
              link
              type="primary"
              :disabled="row.lateSubmitOpen"
              @click="openResubmitDialog(row)"
            >
              开放补交
            </el-button>
            <el-button
              v-permission="'material:manage'"
              link
              type="danger"
              :disabled="!row.lateSubmitOpen"
              @click="closeResubmit(row)"
            >
              关闭补交
            </el-button>
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

    <material-version-history-dialog v-model="versionVisible" :material-id="currentMaterial?.materialId" />

    <el-dialog v-model="resubmitVisible" title="开放补交" width="560px">
      <el-form :model="resubmitForm" label-width="100px">
        <el-form-item label="材料类型">
          <el-input :model-value="currentMaterial?.materialTypeName || '-'" disabled />
        </el-form-item>
        <el-form-item label="补交截止">
          <el-date-picker
            v-model="resubmitForm.openUntil"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="可留空（不限制）"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="开放原因">
          <el-input v-model.trim="resubmitForm.openReason" type="textarea" :rows="3" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resubmitVisible = false">取消</el-button>
        <el-button type="primary" :loading="resubmitSaving" @click="submitResubmit">确认开放</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  closeMaterialResubmitApi,
  fetchMaterialDownloadBlobApi,
  fetchMaterialPreviewBlobApi,
  openMaterialResubmitApi,
  queryAdminMaterialPageApi
} from '@/api/modules/material';
import type { MaterialListItem } from '@/types/api';
import MaterialVersionHistoryDialog from './components/MaterialVersionHistoryDialog.vue';
import { openMaterialVersionPreview } from '@/utils/material-preview';

const loading = ref(false);
const records = ref<MaterialListItem[]>([]);
const total = ref(0);

const query = reactive({
  page: 1,
  size: 10,
  keyword: '',
  studentNo: '',
  studentName: '',
  materialStatus: ''
});

const versionVisible = ref(false);
const currentMaterial = ref<MaterialListItem | null>(null);

const resubmitVisible = ref(false);
const resubmitSaving = ref(false);
const resubmitForm = reactive({
  openUntil: '',
  openReason: ''
});

async function fetchData() {
  loading.value = true;
  try {
    const resp = await queryAdminMaterialPageApi({
      page: query.page,
      size: query.size,
      keyword: query.keyword || undefined,
      studentNo: query.studentNo || undefined,
      studentName: query.studentName || undefined,
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
  query.keyword = '';
  query.studentNo = '';
  query.studentName = '';
  query.materialStatus = '';
  fetchData();
}

function openVersionDialog(row: MaterialListItem) {
  currentMaterial.value = row;
  versionVisible.value = true;
}

function openResubmitDialog(row: MaterialListItem) {
  currentMaterial.value = row;
  resubmitForm.openUntil = '';
  resubmitForm.openReason = '';
  resubmitVisible.value = true;
}

async function submitResubmit() {
  if (!currentMaterial.value) {
    return;
  }
  resubmitSaving.value = true;
  try {
    await openMaterialResubmitApi({
      materialId: currentMaterial.value.materialId,
      openUntil: resubmitForm.openUntil || undefined,
      openReason: resubmitForm.openReason || undefined
    });
    ElMessage.success('补交已开放');
    resubmitVisible.value = false;
    fetchData();
  } finally {
    resubmitSaving.value = false;
  }
}

async function closeResubmit(row: MaterialListItem) {
  await ElMessageBox.confirm('确认关闭该材料的补交通道？', '确认', {
    type: 'warning',
    confirmButtonText: '确认',
    cancelButtonText: '取消'
  });
  await closeMaterialResubmitApi(row.materialId);
  ElMessage.success('已关闭补交');
  fetchData();
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
