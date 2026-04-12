<template>
  <el-dialog v-model="dialogVisible" title="上传材料" width="640px" @closed="handleClosed">
    <template v-if="material">
      <el-descriptions :column="2" border class="meta-box">
        <el-descriptions-item label="材料类型">{{ material.materialTypeName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="截止时间">{{ material.deadlineTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="已提交版本">{{ material.latestVersionNo || 0 }}</el-descriptions-item>
        <el-descriptions-item label="最多提交">{{ material.maxSubmitCount || 1 }}</el-descriptions-item>
      </el-descriptions>

      <el-upload
        class="upload-box"
        :auto-upload="false"
        :limit="1"
        :show-file-list="true"
        accept=".doc,.docx,.pdf,.jpg,.jpeg,.png,.mp4"
        :on-change="handleFileChange"
        :on-remove="handleFileRemove"
        :before-upload="beforeUpload"
      >
        <el-button type="primary">选择文件</el-button>
        <template #tip>
          <div class="el-upload__tip">支持 doc/docx/pdf/jpg/png/mp4，单文件不超过 20MB。</div>
        </template>
      </el-upload>

      <el-form :model="form" label-width="90px" class="remark-form">
        <el-form-item label="提交说明">
          <el-input v-model.trim="form.submitRemark" type="textarea" :rows="3" maxlength="300" show-word-limit />
        </el-form-item>
      </el-form>

      <el-progress v-if="uploading" :percentage="progress" :stroke-width="14" />
    </template>

    <template #footer>
      <el-button :disabled="uploading" @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" :loading="uploading" @click="handleSubmit">上传</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { computed, reactive, ref, watch } from 'vue';
import { ElMessage, type UploadFile, type UploadRawFile } from 'element-plus';
import { uploadStudentMaterialApi } from '@/api/modules/material';
import type { IdValue, MaterialListItem, MaterialVersionItem } from '@/types/api';

const props = withDefaults(
  defineProps<{
    modelValue: boolean;
    material: MaterialListItem | null;
  }>(),
  {
    material: null
  }
);

const emit = defineEmits<{
  (event: 'update:modelValue', value: boolean): void;
  (event: 'success', payload: { materialId: IdValue; version: MaterialVersionItem }): void;
}>();

const form = reactive({
  submitRemark: ''
});

const selectedFile = ref<File | null>(null);
const uploading = ref(false);
const progress = ref(0);

const dialogVisible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
});

watch(
  () => props.modelValue,
  (value) => {
    if (value) {
      resetForm();
    }
  }
);

function validateFile(file: { name: string; size: number }) {
  const fileName = file.name || '';
  const extension = fileName.includes('.') ? fileName.split('.').pop()?.toLowerCase() || '' : '';
  const allowList = ['doc', 'docx', 'pdf', 'jpg', 'jpeg', 'png', 'mp4'];
  if (!allowList.includes(extension)) {
    ElMessage.error('文件格式不支持');
    return false;
  }

  const maxSize = 20 * 1024 * 1024;
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 20MB');
    return false;
  }
  return true;
}

function beforeUpload(rawFile: UploadRawFile) {
  return validateFile(rawFile);
}

function handleFileChange(file: UploadFile) {
  selectedFile.value = (file.raw as File) || null;
}

function handleFileRemove() {
  selectedFile.value = null;
}

async function handleSubmit() {
  if (!props.material) {
    ElMessage.warning('请选择材料后上传');
    return;
  }
  if (!selectedFile.value) {
    ElMessage.warning('请先选择文件');
    return;
  }
  if (!validateFile(selectedFile.value)) {
    return;
  }
  if (!props.material.canSubmit) {
    ElMessage.warning('当前材料不可提交，请检查截止时间或补交状态');
    return;
  }

  uploading.value = true;
  progress.value = 0;
  try {
    const resp = await uploadStudentMaterialApi({
      materialId: props.material.materialId,
      file: selectedFile.value,
      submitRemark: form.submitRemark || undefined,
      onUploadProgress: (value) => {
        progress.value = value;
      }
    });
    ElMessage.success('材料上传成功');
    emit('success', {
      materialId: props.material.materialId,
      version: resp.data
    });
    dialogVisible.value = false;
  } finally {
    uploading.value = false;
  }
}

function handleClosed() {
  resetForm();
}

function resetForm() {
  selectedFile.value = null;
  form.submitRemark = '';
  progress.value = 0;
  uploading.value = false;
}
</script>

<style scoped lang="scss">
.meta-box {
  margin-bottom: 12px;
}

.upload-box {
  margin-bottom: 12px;
}

.remark-form {
  margin-bottom: 8px;
}
</style>
