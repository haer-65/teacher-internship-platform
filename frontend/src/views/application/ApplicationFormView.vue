<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <span class="title">{{ isEdit ? '编辑申请' : '提交申请' }}</span>
        <el-button @click="goBack">返回</el-button>
      </div>
    </template>

    <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="140px">
      <el-form-item label="实习计划" prop="planId">
        <el-select
          v-model="formModel.planId"
          filterable
          placeholder="请选择实习计划"
          :disabled="isEdit"
          style="width: 100%"
          @change="handlePlanChange"
        >
          <el-option
            v-for="plan in planOptions"
            :key="String(plan.id)"
            :label="`${plan.planName} (${plan.planCode})`"
            :value="plan.id"
          />
        </el-select>
      </el-form-item>

      <template v-if="selectedPlan">
        <el-alert
          :title="`当前计划申请截止时间：${selectedPlan.applyDeadline}`"
          type="info"
          :closable="false"
          show-icon
          class="plan-alert"
        />

        <el-form-item label="实习基地志愿" required>
          <div class="preference-panel">
            <div v-for="(_, index) in preferenceSlots" :key="index" class="preference-row">
              <div class="preference-label">第 {{ index + 1 }} 志愿</div>
              <el-select
                v-model="preferenceSlots[index]"
                filterable
                clearable
                placeholder="请选择实习基地"
                style="width: 100%"
              >
                <el-option
                  v-for="base in selectedPlan.planBases"
                  :key="`${String(base.baseId)}-${index}`"
                  :label="buildBaseLabel(base)"
                  :value="base.baseId"
                  :disabled="isBaseSelectedElsewhere(base.baseId, index) || getRemainingQuota(base) <= 0"
                />
              </el-select>
            </div>
            <div class="preference-tip">至少选择 1 个志愿，最多可填写 3 个志愿，且不能重复。</div>
          </div>
        </el-form-item>

        <el-table :data="selectedPlan.planBases" border class="plan-base-table">
          <el-table-column prop="baseCode" label="基地编号" width="140" />
          <el-table-column prop="baseName" label="基地名称" min-width="220" />
          <el-table-column prop="remainingQuota" label="剩余名额" width="120">
            <template #default="{ row }">
              {{ getRemainingQuota(row) }}
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" min-width="180" />
        </el-table>
      </template>

      <el-form-item label="个人陈述" prop="personalStatement">
        <el-input
          v-model.trim="formModel.personalStatement"
          type="textarea"
          :rows="6"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ isEdit ? '保存修改' : '提交申请' }}
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import {
  getStudentApplicationDetailApi,
  queryStudentAvailablePlansApi,
  submitStudentApplicationApi,
  updateStudentApplicationApi
} from '@/api/modules/application';
import type { StudentApplicationSaveRequest } from '@/api/modules/application';
import type { IdValue, PlanBaseItem, StudentAvailablePlanItem } from '@/types/api';
import { normalizeRouteId } from '@/utils/id';

const route = useRoute();
const router = useRouter();
const formRef = ref<FormInstance>();
const saving = ref(false);
const planOptions = ref<StudentAvailablePlanItem[]>([]);

const applicationId = computed(() => normalizeRouteId(route.params.id));
const isEdit = computed(() => Boolean(applicationId.value));

const formModel = reactive<StudentApplicationSaveRequest>({
  planId: '',
  preferredBaseIds: [],
  personalStatement: ''
});

const preferenceSlots = reactive<Array<IdValue | ''>>(['', '', '']);

const selectedPlan = computed(() =>
  planOptions.value.find((item) => String(item.id) === String(formModel.planId))
);

const formRules: FormRules = {
  planId: [{ required: true, message: '请选择实习计划', trigger: 'change' }]
};

function syncPreferenceSlots(source: IdValue[] = []) {
  for (let index = 0; index < 3; index += 1) {
    preferenceSlots[index] = source[index] ?? '';
  }
}

function collectPreferredBaseIds(): IdValue[] {
  const result: IdValue[] = [];
  const seen = new Set<string>();
  for (const value of preferenceSlots) {
    if (value === '' || value === null || value === undefined) {
      continue;
    }
    const key = String(value);
    if (seen.has(key)) {
      throw new Error('实习基地志愿不能重复选择');
    }
    seen.add(key);
    result.push(value);
  }
  if (!result.length) {
    throw new Error('请至少选择 1 个实习基地志愿');
  }
  return result;
}

function isBaseSelectedElsewhere(baseId: IdValue, currentIndex: number) {
  return preferenceSlots.some((value, index) => index !== currentIndex && String(value) === String(baseId));
}

function buildBaseLabel(base: PlanBaseItem) {
  const quotaText = ` / 剩余：${getRemainingQuota(base)}`;
  return `${base.baseName || '-'}${base.baseCode ? ` (${base.baseCode})` : ''}${quotaText}`;
}

function getRemainingQuota(base: PlanBaseItem) {
  if (typeof base.remainingQuota === 'number') {
    return base.remainingQuota;
  }
  return typeof base.baseQuota === 'number' ? base.baseQuota : 0;
}

async function loadPlanOptions() {
  const resp = await queryStudentAvailablePlansApi();
  planOptions.value = resp.data || [];
}

async function loadDetail() {
  if (!isEdit.value) {
    return;
  }
  const resp = await getStudentApplicationDetailApi(applicationId.value);
  formModel.planId = resp.data.planId || '';
  formModel.personalStatement = resp.data.personalStatement || '';
  formModel.preferredBaseIds = (resp.data.preferredBases || []).map((item) => item.baseId);
  syncPreferenceSlots(formModel.preferredBaseIds);
}

function handlePlanChange() {
  if (isEdit.value) {
    return;
  }
  syncPreferenceSlots([]);
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }

  try {
    formModel.preferredBaseIds = collectPreferredBaseIds();
  } catch (error) {
    ElMessage.error(error instanceof Error ? error.message : '志愿信息校验失败');
    return;
  }

  saving.value = true;
  try {
    if (isEdit.value) {
      await updateStudentApplicationApi(applicationId.value, formModel);
      ElMessage.success('申请已保存');
    } else {
      await submitStudentApplicationApi(formModel);
      ElMessage.success('申请已提交');
    }
    await router.replace('/application/student');
  } finally {
    saving.value = false;
  }
}

function goBack() {
  router.back();
}

onMounted(async () => {
  await loadPlanOptions();
  if (!isEdit.value) {
    const queryPlanId = normalizeRouteId(route.query.planId);
    if (queryPlanId) {
      formModel.planId = queryPlanId;
    }
  }
  await loadDetail();
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

.plan-alert {
  margin-bottom: 16px;
}

.preference-panel {
  width: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preference-row {
  display: grid;
  grid-template-columns: 88px minmax(0, 1fr);
  gap: 12px;
  align-items: center;
}

.preference-label {
  color: #606266;
}

.preference-tip {
  color: #909399;
  font-size: 13px;
}

.plan-base-table {
  margin-bottom: 18px;
}
</style>
