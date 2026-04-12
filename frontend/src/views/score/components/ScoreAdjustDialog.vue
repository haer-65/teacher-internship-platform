<template>
  <el-dialog
    :model-value="modelValue"
    title="调整成绩"
    width="520px"
    @close="handleClose"
  >
    <el-alert
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 12px"
      :title="`当前成绩：${displayScore(currentScore)}`"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="120px">
      <el-form-item label="新总分" prop="newTotalScore">
        <el-input-number
          v-model="form.newTotalScore"
          :min="0"
          :max="100"
          :precision="2"
          :step="1"
          style="width: 220px"
        />
      </el-form-item>
      <el-form-item label="调整原因" prop="adjustReason">
        <el-input
          v-model.trim="form.adjustReason"
          type="textarea"
          :rows="3"
          maxlength="300"
          show-word-limit
          placeholder="请输入成绩调整原因"
        />
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button
        v-permission="'score:adjust'"
        type="primary"
        :loading="submitting"
        @click="handleSubmit"
      >
        确认调整
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { reactive, ref, watch } from 'vue';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { adjustScoreApi } from '@/api/modules/score';
import type { IdValue } from '@/types/api';

const props = defineProps<{
  modelValue: boolean;
  scoreSheetId?: IdValue;
  currentScore?: number;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void;
  (e: 'success'): void;
}>();

const submitting = ref(false);
const formRef = ref<FormInstance>();
const form = reactive({
  newTotalScore: undefined as number | undefined,
  adjustReason: ''
});

const rules: FormRules = {
  newTotalScore: [{ required: true, message: '请输入分数', trigger: 'change' }],
  adjustReason: [{ required: true, message: '请输入原因', trigger: 'blur' }]
};

watch(
  () => props.modelValue,
  (visible) => {
    if (!visible) {
      return;
    }
    form.newTotalScore = props.currentScore;
    form.adjustReason = '';
  }
);

function handleClose() {
  emit('update:modelValue', false);
}

async function handleSubmit() {
  if (!props.scoreSheetId) {
    ElMessage.error('缺少 scoreSheetId 参数');
    return;
  }
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }
  submitting.value = true;
  try {
    await adjustScoreApi(props.scoreSheetId, {
      newTotalScore: Number(form.newTotalScore),
      adjustReason: form.adjustReason
    });
    ElMessage.success('成绩调整成功');
    emit('success');
    emit('update:modelValue', false);
  } finally {
    submitting.value = false;
  }
}

function displayScore(value?: number): string {
  if (value === undefined || value === null || Number.isNaN(value)) {
    return '0.00';
  }
  return Number(value).toFixed(2);
}
</script>
