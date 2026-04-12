<template>
  <div class="attachment-uploader">
    <el-upload
      v-if="!readonly"
      class="upload-box"
      :show-file-list="false"
      :http-request="handleUpload"
      :disabled="uploading || !planId"
    >
      <el-button type="primary" :loading="uploading" :disabled="!planId">上传附件</el-button>
      <template #tip>
        <div class="el-upload__tip">
          {{ planId ? '支持常见办公文档上传。' : '请先保存计划，再上传附件。' }}
        </div>
      </template>
    </el-upload>

    <el-table :data="attachments" border>
      <el-table-column prop="fileName" label="文件名" min-width="220" />
      <el-table-column prop="fileSize" label="文件大小" width="120">
        <template #default="{ row }">{{ formatSize(row.fileSize) }}</template>
      </el-table-column>
      <el-table-column prop="uploadedTime" label="上传时间" min-width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleDownload(row)">下载</el-button>
          <el-button v-if="!readonly" type="danger" link @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { ElMessage, ElMessageBox, type UploadRequestOptions } from 'element-plus';
import { deletePlanAttachmentApi, downloadPlanAttachmentApi, uploadPlanAttachmentApi } from '@/api/modules/plan';
import type { IdValue, PlanAttachmentItem } from '@/types/api';

const props = withDefaults(
  defineProps<{
    planId?: IdValue;
    attachments: PlanAttachmentItem[];
    readonly?: boolean;
  }>(),
  {
    planId: undefined,
    readonly: false
  }
);

const emit = defineEmits<{
  (event: 'uploaded', attachment: PlanAttachmentItem): void;
  (event: 'deleted', attachmentId: IdValue): void;
}>();

const uploading = ref(false);

async function handleUpload(options: UploadRequestOptions) {
  if (!props.planId) {
    ElMessage.warning('请先保存计划。');
    return;
  }

  uploading.value = true;
  try {
    const resp = await uploadPlanAttachmentApi(props.planId, options.file as File);
    emit('uploaded', resp.data);
    ElMessage.success('附件上传成功。');
    options.onSuccess(resp.data as never);
  } catch (error) {
    options.onError(error as never);
  } finally {
    uploading.value = false;
  }
}

async function handleDelete(attachmentId: IdValue) {
  await ElMessageBox.confirm('确认删除该附件吗？', '提示', { type: 'warning' });
  await deletePlanAttachmentApi(attachmentId);
  emit('deleted', attachmentId);
  ElMessage.success('附件删除成功。');
}

function handleDownload(row: PlanAttachmentItem) {
  void downloadAttachment(row);
}

async function downloadAttachment(row: PlanAttachmentItem) {
  try {
    const response = await downloadPlanAttachmentApi(row.id);
    const blob = new Blob([response.data], { type: response.headers?.['content-type'] || 'application/octet-stream' });
    const url = window.URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = url;
    anchor.download = row.fileName || 'attachment';
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    window.URL.revokeObjectURL(url);
  } catch (error) {
    console.error('附件下载失败:', error);
  }
}

function formatSize(size: number): string {
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

<style scoped lang="scss">
.attachment-uploader {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.upload-box {
  display: inline-block;
}
</style>
