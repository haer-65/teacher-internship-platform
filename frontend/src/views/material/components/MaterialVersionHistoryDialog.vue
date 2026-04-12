<template>
  <el-dialog v-model="dialogVisible" title="版本历史" width="920px" @open="fetchData">
    <el-table v-loading="loading" :data="records" border>
      <el-table-column prop="versionNo" label="版本" width="90" />
      <el-table-column prop="fileName" label="文件名" min-width="220" />
      <el-table-column prop="fileSize" label="大小" width="110">
        <template #default="{ row }">{{ formatFileSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column prop="submittedByName" label="上传人" width="120" />
      <el-table-column prop="submittedTime" label="上传时间" width="170" />
      <el-table-column prop="submitRemark" label="提交说明" min-width="160" />
      <el-table-column prop="isCurrent" label="当前版本" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isCurrent === 1 ? 'success' : 'info'">{{ row.isCurrent === 1 ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" :disabled="!row.previewable" @click="handlePreview(row)">预览</el-button>
          <el-button link type="success" @click="handleDownload(row)">下载</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';
import {
  fetchMaterialDownloadBlobApi,
  fetchMaterialPreviewBlobApi,
  queryMaterialVersionListApi
} from '@/api/modules/material';
import type { IdValue, MaterialVersionItem } from '@/types/api';
import { openMaterialVersionPreview } from '@/utils/material-preview';

const props = defineProps<{
  modelValue: boolean;
  materialId?: IdValue;
}>();

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void;
}>();

const loading = ref(false);
const records = ref<MaterialVersionItem[]>([]);

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
});

async function fetchData() {
  if (!props.materialId) {
    records.value = [];
    return;
  }
  loading.value = true;
  try {
    const resp = await queryMaterialVersionListApi(props.materialId);
    records.value = resp.data || [];
  } finally {
    loading.value = false;
  }
}

async function handlePreview(row: MaterialVersionItem) {
  if (!row.previewable) {
    ElMessage.warning('该格式不支持在线预览，请下载查看');
    return;
  }
  await openMaterialVersionPreview(row, () => fetchMaterialPreviewBlobApi(row.id));
}

async function handleDownload(row: MaterialVersionItem) {
  const blob = await fetchMaterialDownloadBlobApi(row.id);
  const objectUrl = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = objectUrl;
  a.download = row.fileName || `材料_v${row.versionNo}`;
  document.body.appendChild(a);
  a.click();
  document.body.removeChild(a);
  URL.revokeObjectURL(objectUrl);
}

function formatFileSize(size: number): string {
  if (!size) {
    return '0 B';
  }
  if (size < 1024) {
    return `${size} B`;
  }
  if (size < 1024 * 1024) {
    return `${(size / 1024).toFixed(1)} KB`;
  }
  return `${(size / (1024 * 1024)).toFixed(1)} MB`;
}
</script>
