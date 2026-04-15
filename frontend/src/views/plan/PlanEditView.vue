<template>
  <el-card>
    <template #header>
      <div class="header-row">
        <div class="title">{{ isEdit ? '编辑计划草稿' : '新建计划草稿' }}</div>
        <el-button @click="goBack">返回列表</el-button>
      </div>
    </template>

    <el-form ref="formRef" :model="formModel" :rules="formRules" label-width="120px" class="plan-form">
      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="计划编码">
            <div class="plan-code-display">{{ planCodeDisplay }}</div>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="计划名称" prop="planName">
            <el-input v-model.trim="formModel.planName" :disabled="!editable" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学年" prop="academicYear">
            <el-select v-model="formModel.academicYear" class="plan-select" placeholder="请选择学年" :disabled="!editable" clearable>
              <el-option v-for="item in academicYearOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学期" prop="term">
            <el-select v-model="formModel.term" class="plan-select" placeholder="请选择学期" :disabled="!editable" clearable>
              <el-option v-for="item in termOptions" :key="item" :label="item" :value="item" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="院系" prop="deptId">
            <el-select v-model="formModel.deptId" class="plan-select" placeholder="请选择院系" :disabled="!editable" clearable>
              <el-option v-for="item in departmentOptions" :key="String(item.id)" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="学生名额" prop="studentQuota">
            <el-input-number v-model="formModel.studentQuota" :min="1" :disabled="!editable" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="formModel.startTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled="!editable"
              style="width: 100%"
              popper-class="plan-datetime-popper"
            />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="结束时间" prop="endTime">
            <el-date-picker
              v-model="formModel.endTime"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled="!editable"
              style="width: 100%"
              popper-class="plan-datetime-popper"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="报名截止时间" prop="applyDeadline">
            <el-date-picker
              v-model="formModel.applyDeadline"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              :disabled="!editable"
              style="width: 100%"
              popper-class="plan-datetime-popper"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="计划说明">
            <el-input v-model="formModel.description" type="textarea" :rows="4" :disabled="!editable" placeholder="补充说明实习安排、实习基地选择规则和特殊要求" />
          </el-form-item>
        </el-col>
      </el-row>

      <el-alert
        :type="teacherWeightTotal === 100 ? 'success' : 'warning'"
        :closable="false"
        :title="`双导师权重总和 ${teacherWeightTotal}%`"
        show-icon
        class="summary-alert"
      />

      <el-row :gutter="16">
        <el-col :span="12">
          <el-form-item label="校内导师权重">
            <el-input-number v-model="formModel.innerTeacherWeight" :min="0" :max="100" :step="0.5" :disabled="!editable" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="基地导师权重">
            <el-input-number v-model="formModel.baseTeacherWeight" :min="0" :max="100" :step="0.5" :disabled="!editable" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>

    <el-divider>计划可选实习基地</el-divider>
    <div class="toolbar-row">
      <el-button v-if="editable" type="primary" plain @click="addPlanBaseRow">新增基地</el-button>
      <div class="weight-tip">启用基地名额合计：{{ enabledBaseQuotaTotal }}/{{ formModel.studentQuota }}</div>
    </div>

    <el-table :data="planBases" border>
      <el-table-column label="实习基地" min-width="220">
        <template #default="{ row }">
          <el-select v-model="row.baseId" filterable clearable placeholder="请选择实习基地" :disabled="!editable" style="width: 100%">
            <el-option v-for="item in baseOptions" :key="String(item.id)" :label="item.name" :value="item.id" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="基地名额" width="140">
        <template #default="{ row }">
          <el-input-number v-model="row.baseQuota" :min="1" :disabled="!editable" style="width: 100%" />
        </template>
      </el-table-column>
      <el-table-column label="排序" width="120">
        <template #default="{ row }">
          <el-input-number v-model="row.sortNo" :min="1" :disabled="!editable" style="width: 100%" />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-select v-model="row.status" :disabled="!editable" style="width: 100%">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="备注" min-width="180">
        <template #default="{ row }">
          <el-input v-model.trim="row.remark" :disabled="!editable" />
        </template>
      </el-table-column>
      <el-table-column v-if="editable" label="操作" width="90" fixed="right">
        <template #default="{ $index }">
          <el-button type="danger" link @click="removePlanBaseRow($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-divider>材料类型配置</el-divider>
    <div class="toolbar-row">
      <el-button v-if="editable" type="primary" plain @click="addMaterialRow">新增材料类型</el-button>
      <div class="weight-tip">材料权重总和：{{ materialWeightTotal }}%</div>
    </div>
    <el-alert
      :type="materialWeightTotal === 100 ? 'success' : 'warning'"
      :closable="false"
      :title="`材料权重总和 ${materialWeightTotal}%`"
      show-icon
      class="summary-alert summary-alert--material"
    />

    <el-table :data="materialTypes" border>
      <el-table-column label="材料类型" min-width="180">
        <template #default="{ row }">
          <el-select
            v-model="row.typeName"
            filterable
            allow-create
            default-first-option
            clearable
            placeholder="请选择或输入材料类型"
            :disabled="!editable"
            style="width: 100%"
            @change="handleMaterialTypeChange(row)"
            @blur="handleMaterialTypeChange(row)"
          >
            <el-option
              v-for="item in materialTypeOptions"
              :key="item.code"
              :label="item.name"
              :value="item.name"
            />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column label="类型编码" min-width="140">
        <template #default="{ row }">
          <el-input v-model.trim="row.typeCode" disabled placeholder="选择后自动生成" />
        </template>
      </el-table-column>
      <el-table-column label="必交" width="90">
        <template #default="{ row }">
          <el-switch v-model="row.requiredFlag" :active-value="1" :inactive-value="0" :disabled="!editable" />
        </template>
      </el-table-column>
      <el-table-column label="截止时间" min-width="180">
        <template #default="{ row }">
          <el-date-picker
            v-model="row.deadlineTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            :disabled="!editable"
            style="width: 100%"
            popper-class="plan-datetime-popper"
          />
        </template>
      </el-table-column>
      <el-table-column label="权重(%)" width="130">
        <template #default="{ row }">
          <el-input-number v-model="row.weight" :min="0" :max="100" :step="0.5" :disabled="!editable" style="width: 100%" />
        </template>
      </el-table-column>
      <el-table-column label="允许补交" width="120">
        <template #default="{ row }">
          <el-switch v-model="row.allowResubmit" :disabled="!editable" />
        </template>
      </el-table-column>
      <el-table-column label="最大提交次数" width="130">
        <template #default="{ row }">
          <el-input-number v-model="row.maxSubmitCount" :min="1" :max="50" :disabled="!editable || !row.allowResubmit" style="width: 100%" />
        </template>
      </el-table-column>
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-select v-model="row.status" :disabled="!editable" style="width: 100%">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </template>
      </el-table-column>
      <el-table-column v-if="editable" label="操作" width="90" fixed="right">
        <template #default="{ $index }">
          <el-button type="danger" link @click="removeMaterialRow($index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-divider>附件</el-divider>
    <plan-attachment-uploader
      :plan-id="planId"
      :attachments="attachments"
      :readonly="!editable"
      @uploaded="handleAttachmentUploaded"
      @deleted="handleAttachmentDeleted"
    />

    <div class="footer-row">
      <el-button @click="goBack">取消</el-button>
      <el-button v-if="editable" type="primary" :loading="saving" @click="handleSave">保存草稿</el-button>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import PlanAttachmentUploader from './components/PlanAttachmentUploader.vue';
import { createPlanApi, getPlanDetailApi, queryPlanBaseOptionsApi, queryPlanDepartmentsApi, updatePlanApi } from '@/api/modules/plan';
import { querySystemParamValueApi } from '@/api/modules/system-admin';
import type { IdNameOption, IdValue, PlanAttachmentItem, PlanBaseItem, PlanDetailData, PlanMaterialTypeItem } from '@/types/api';
import { normalizeRouteId } from '@/utils/id';

interface MaterialTypeOption {
  code: string;
  name: string;
}

interface PlanFormModel {
  planCode: string;
  planName: string;
  academicYear: string;
  term: string;
  deptId: IdValue | undefined;
  startTime: string;
  endTime: string;
  applyDeadline: string;
  studentQuota: number;
  description: string;
  innerTeacherWeight: number;
  baseTeacherWeight: number;
}

const route = useRoute();
const router = useRouter();
const formRef = ref<FormInstance>();
const saving = ref(false);
const planId = ref<IdValue | undefined>(undefined);
const planStatus = ref('DRAFT');
const attachments = ref<PlanAttachmentItem[]>([]);
const materialTypes = ref<PlanMaterialTypeItem[]>([]);
const planBases = ref<PlanBaseItem[]>([]);
const departmentOptions = ref<IdNameOption[]>([]);
const baseOptions = ref<IdNameOption[]>([]);
const academicYearOptions = Array.from({ length: 5 }, (_, index) => {
  const year = new Date().getFullYear() - 1 + index;
  return `${year}-${year + 1}`;
});
const termOptions = ['春季学期', '秋季学期'];
const planCodeDisplay = computed(() => formModel.planCode || '保存后自动生成');
const materialTypeOptions: MaterialTypeOption[] = [
  { code: 'WEEKLY_LOG', name: '实习周志' },
  { code: 'MID_REPORT', name: '中期报告' },
  { code: 'FINAL_REPORT', name: '实习总结报告' },
  { code: 'LESSON_PLAN', name: '教案' },
  { code: 'LESSON_RECORD', name: '听评课记录' }
];

const formModel = reactive<PlanFormModel>({
  planCode: '',
  planName: '',
  academicYear: '',
  term: '',
  deptId: undefined,
  startTime: '',
  endTime: '',
  applyDeadline: '',
  studentQuota: 50,
  description: '',
  innerTeacherWeight: 50,
  baseTeacherWeight: 50
});

const formRules: FormRules = {
  planName: [{ required: true, message: '请输入计划名称', trigger: 'blur' }],
  academicYear: [{ required: true, message: '请选择学年', trigger: 'change' }],
  term: [{ required: true, message: '请选择学期', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择院系', trigger: 'change' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
  applyDeadline: [{ required: true, message: '请选择报名截止时间', trigger: 'change' }],
  studentQuota: [{ required: true, message: '请输入学生名额', trigger: 'change' }]
};

const isEdit = computed(() => Boolean(route.params.id));
const editable = computed(() => planStatus.value === 'DRAFT');
const teacherWeightTotal = computed(() => Number((Number(formModel.innerTeacherWeight) + Number(formModel.baseTeacherWeight)).toFixed(2)));
const materialWeightTotal = computed(() => Number(materialTypes.value.reduce((sum, item) => sum + Number(item.weight || 0), 0).toFixed(2)));
const enabledBaseQuotaTotal = computed(() =>
  planBases.value.reduce((sum, item) => sum + (item.status === 'ENABLED' ? Number(item.baseQuota || 0) : 0), 0)
);

function createMaterialRow(): PlanMaterialTypeItem {
  return {
    typeCode: '',
    typeName: '',
    requiredFlag: 1,
    allowResubmit: false,
    maxSubmitCount: 1,
    deadlineTime: '',
    weight: 0,
    status: 'ENABLED'
  };
}

function createPlanBaseRow(): PlanBaseItem {
  return {
    baseId: '',
    baseQuota: 1,
    sortNo: planBases.value.length + 1,
    status: 'ENABLED',
    remark: ''
  };
}

function normalizeMaterialTypeCode(text?: string): string {
  return text?.trim().toUpperCase() || '';
}

function findMaterialTypeOptionByCode(code?: string): MaterialTypeOption | undefined {
  const normalizedCode = normalizeMaterialTypeCode(code);
  return materialTypeOptions.find((item) => item.code === normalizedCode);
}

function findMaterialTypeOptionByName(name?: string): MaterialTypeOption | undefined {
  const normalizedName = name?.trim() || '';
  return materialTypeOptions.find((item) => item.name === normalizedName);
}

function buildCustomMaterialTypeCode(typeName?: string): string {
  const normalizedName = typeName?.trim() || '';
  if (!normalizedName) {
    return '';
  }

  const asciiText = normalizedName
    .normalize('NFKD')
    .replace(/[^\x00-\x7F]/g, ' ')
    .replace(/[^A-Za-z0-9]+/g, '_')
    .replace(/^_+|_+$/g, '')
    .toUpperCase();

  if (asciiText) {
    return `CUSTOM_${asciiText.slice(0, 24)}`;
  }

  let hash = 0;
  for (const char of normalizedName) {
    hash = ((hash * 31) + char.codePointAt(0)!) >>> 0;
  }
  return `CUSTOM_${hash.toString(36).toUpperCase()}`;
}

function normalizeMaterialTypeRow(item: PlanMaterialTypeItem): PlanMaterialTypeItem {
  if (!item.typeName?.trim()) {
    return {
      ...item,
      typeCode: '',
      typeName: ''
    };
  }

  const matchedOption = findMaterialTypeOptionByName(item.typeName) || findMaterialTypeOptionByCode(item.typeCode);
  const typeName = matchedOption?.name || item.typeName.trim();
  const typeCode = matchedOption?.code || normalizeMaterialTypeCode(item.typeCode) || buildCustomMaterialTypeCode(typeName);

  return {
    ...item,
    typeCode,
    typeName
  };
}

function syncMaterialTypeRows() {
  materialTypes.value = materialTypes.value.map((item) => normalizeMaterialTypeRow(item));
}

function handleMaterialTypeChange(row: PlanMaterialTypeItem) {
  Object.assign(row, normalizeMaterialTypeRow(row));
}

function addMaterialRow() {
  materialTypes.value.push(createMaterialRow());
}

function removeMaterialRow(index: number) {
  materialTypes.value.splice(index, 1);
}

function addPlanBaseRow() {
  planBases.value.push(createPlanBaseRow());
}

function removePlanBaseRow(index: number) {
  planBases.value.splice(index, 1);
  planBases.value.forEach((item, rowIndex) => {
    item.sortNo = rowIndex + 1;
  });
}

async function loadDetail(id: string) {
  const resp = await getPlanDetailApi(id);
  fillFormByDetail(resp.data);
}

async function fetchDepartmentOptions() {
  const resp = await queryPlanDepartmentsApi();
  departmentOptions.value = resp.data || [];
}

async function fetchBaseOptions() {
  const resp = await queryPlanBaseOptionsApi();
  baseOptions.value = resp.data || [];
}

async function fetchDefaultStudentQuota() {
  try {
    const resp = await querySystemParamValueApi('PLAN_DEFAULT_QUOTA', '50');
    const parsed = Number(resp.data || 0);
    if (!Number.isNaN(parsed) && parsed > 0 && !isEdit.value) {
      formModel.studentQuota = parsed;
    }
  } catch (error) {
    console.error('获取默认计划名额失败:', error);
  }
}

function fillFormByDetail(detail: PlanDetailData) {
  planId.value = detail.id;
  planStatus.value = detail.planStatus;
  formModel.planCode = detail.planCode;
  formModel.planName = detail.planName;
  formModel.academicYear = detail.academicYear;
  formModel.term = detail.term;
  formModel.deptId = detail.deptId;
  formModel.startTime = detail.startTime;
  formModel.endTime = detail.endTime;
  formModel.applyDeadline = detail.applyDeadline;
  formModel.studentQuota = detail.studentQuota;
  formModel.description = detail.description || '';
  formModel.innerTeacherWeight = Number(detail.innerTeacherWeight);
  formModel.baseTeacherWeight = Number(detail.baseTeacherWeight);
  planBases.value = (detail.planBases || []).map((item, index) => ({
    ...item,
    baseQuota: Number(item.baseQuota || 0),
    sortNo: Number(item.sortNo || index + 1),
    status: item.status || 'ENABLED',
    remark: item.remark || ''
  }));
  materialTypes.value = (detail.materialTypes || []).map((item) => ({
    ...normalizeMaterialTypeRow(item),
    weight: Number(item.weight),
    allowResubmit: Boolean(item.allowResubmit),
    maxSubmitCount: Number(item.maxSubmitCount || 1)
  }));
  attachments.value = detail.attachments || [];
}

function normalizeMaterialRows(): PlanMaterialTypeItem[] {
  return materialTypes.value.map((item) => ({
    ...normalizeMaterialTypeRow(item),
    weight: Number(item.weight),
    requiredFlag: Number(item.requiredFlag),
    allowResubmit: Boolean(item.allowResubmit),
    maxSubmitCount: item.allowResubmit ? Number(item.maxSubmitCount || 10) : 1,
    status: item.status || 'ENABLED'
  }));
}

function normalizePlanBaseRows(): PlanBaseItem[] {
  return planBases.value.map((item, index) => ({
    ...item,
    baseQuota: Number(item.baseQuota || 0),
    sortNo: Number(item.sortNo || index + 1),
    status: item.status || 'ENABLED',
    remark: item.remark?.trim() || ''
  }));
}

function validateBeforeSave(): boolean {
  syncMaterialTypeRows();
  if (teacherWeightTotal.value !== 100) {
    ElMessage.error('双导师权重总和必须等于 100%。');
    return false;
  }
  if (!planBases.value.length) {
    ElMessage.error('至少需要配置一个可选实习基地。');
    return false;
  }
  if (!materialTypes.value.length) {
    ElMessage.error('至少需要配置一种材料类型。');
    return false;
  }
  if (materialWeightTotal.value !== 100) {
    ElMessage.error('材料权重总和必须等于 100%。');
    return false;
  }

  const baseIdSet = new Set<string>();
  for (const row of planBases.value) {
    const baseId = String(row.baseId || '');
    if (!baseId) {
      ElMessage.error('请为每一行选择实习基地。');
      return false;
    }
    if (baseIdSet.has(baseId)) {
      ElMessage.error('同一个实习基地不能重复配置。');
      return false;
    }
    baseIdSet.add(baseId);
    if (!row.baseQuota || Number(row.baseQuota) <= 0) {
      ElMessage.error('基地名额必须大于 0。');
      return false;
    }
  }
  if (enabledBaseQuotaTotal.value !== Number(formModel.studentQuota || 0)) {
    ElMessage.error('启用基地名额合计必须与学生名额一致。');
    return false;
  }

  const typeCodeSet = new Set<string>();
  for (const row of materialTypes.value) {
    if (!row.typeName || !row.deadlineTime) {
      ElMessage.error('材料类型和截止时间不能为空。');
      return false;
    }
    const codeKey = row.typeCode.trim().toUpperCase();
    if (typeCodeSet.has(codeKey)) {
      ElMessage.error(`重复的材料类型编码：${row.typeCode}`);
      return false;
    }
    typeCodeSet.add(codeKey);
  }

  return true;
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false);
  if (!valid || !validateBeforeSave()) {
    return;
  }

  saving.value = true;
  try {
    const payload = {
      ...formModel,
      deptId: formModel.deptId as IdValue,
      planBases: normalizePlanBaseRows(),
      materialTypes: normalizeMaterialRows()
    };

    if (planId.value) {
      const resp = await updatePlanApi(planId.value, payload);
      fillFormByDetail(resp.data);
      ElMessage.success('草稿更新成功。');
      return;
    }

    const resp = await createPlanApi(payload);
    fillFormByDetail(resp.data);
    ElMessage.success('草稿创建成功。');
    await router.replace(`/plan/edit/${resp.data.id}`);
  } finally {
    saving.value = false;
  }
}

function handleAttachmentUploaded(attachment: PlanAttachmentItem) {
  attachments.value = [attachment, ...attachments.value];
}

function handleAttachmentDeleted(attachmentId: IdValue) {
  attachments.value = attachments.value.filter((item) => String(item.id) !== String(attachmentId));
}

function goBack() {
  router.push('/plan/manage');
}

onMounted(async () => {
  await Promise.all([fetchDepartmentOptions(), fetchBaseOptions(), fetchDefaultStudentQuota()]);
  if (isEdit.value) {
    const id = normalizeRouteId(route.params.id);
    if (id) {
      await loadDetail(id);
      return;
    }
  }
  planBases.value = [createPlanBaseRow()];
  materialTypes.value = [createMaterialRow()];
});
</script>

<style scoped lang="scss">
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.title {
  font-size: 20px;
  font-weight: 600;
}

.summary-alert {
  margin-bottom: 16px;
}

.summary-alert--material {
  margin-top: 8px;
}

.plan-form {
  :deep(.el-form-item__content) {
    min-width: 0;
  }

  :deep(.el-input),
  :deep(.el-input-number),
  :deep(.el-date-editor),
  :deep(.el-select) {
    width: 100%;
  }

  :deep(.el-select__wrapper),
  :deep(.el-input__wrapper) {
    width: 100%;
    box-sizing: border-box;
  }

  :deep(.el-date-editor .el-input__wrapper) {
    min-height: 40px;
    border-radius: 10px;
    box-shadow: inset 0 0 0 1px #d7dee8;
    background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
    transition: box-shadow 0.2s ease, transform 0.2s ease;
  }

  :deep(.el-date-editor .el-input__wrapper:hover) {
    box-shadow: inset 0 0 0 1px #93c5fd;
  }

  :deep(.el-date-editor.is-focused .el-input__wrapper),
  :deep(.el-date-editor.is-focus .el-input__wrapper) {
    box-shadow: inset 0 0 0 1px #3b82f6, 0 0 0 3px rgba(59, 130, 246, 0.12);
  }
}

.plan-select {
  display: block;
  width: 100%;
}

.plan-code-display {
  width: 100%;
  min-height: 40px;
  display: flex;
  align-items: center;
  padding: 0 14px;
  border: 1px solid #d9e2f2;
  border-radius: 10px;
  background: linear-gradient(180deg, #fbfcff 0%, #f4f7ff 100%);
  color: #667085;
  font-size: 14px;
  line-height: 1.4;
  box-sizing: border-box;
}

.toolbar-row {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.weight-tip {
  color: #606266;
  font-size: 14px;
}

.footer-row {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

:global(.plan-datetime-popper) {
  padding: 0;
  border: 1px solid rgba(148, 163, 184, 0.22);
  border-radius: 18px;
  overflow: hidden;
  background: linear-gradient(180deg, #ffffff 0%, #fbfdff 100%);
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.16);
}

:global(.plan-datetime-popper .el-picker-panel) {
  border: none;
  background: transparent;
}

:global(.plan-datetime-popper .el-picker-panel__body-wrapper) {
  padding: 10px 12px 8px;
}

:global(.plan-datetime-popper .el-date-picker__header) {
  margin: 0 0 10px;
  color: #0f172a;
  font-weight: 600;
}

:global(.plan-datetime-popper .el-date-picker__header-label) {
  font-size: 16px;
  letter-spacing: 0.2px;
}

:global(.plan-datetime-popper .el-picker-panel__icon-btn) {
  color: #64748b;
  transition: color 0.2s ease, transform 0.2s ease;
}

:global(.plan-datetime-popper .el-picker-panel__icon-btn:hover) {
  color: #2563eb;
  transform: translateY(-1px);
}

:global(.plan-datetime-popper .el-date-table th) {
  color: #64748b;
  font-weight: 500;
}

:global(.plan-datetime-popper .el-date-table td) {
  padding: 2px 0;
}

:global(.plan-datetime-popper .el-date-table td .el-date-table-cell) {
  padding: 2px 0;
}

:global(.plan-datetime-popper .el-date-table td .el-date-table-cell__text) {
  width: 32px;
  height: 32px;
  line-height: 32px;
  border-radius: 999px;
  transition: background-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

:global(.plan-datetime-popper .el-date-table td.available:hover .el-date-table-cell__text) {
  color: #1d4ed8;
  background: rgba(59, 130, 246, 0.08);
}

:global(.plan-datetime-popper .el-date-table td.current:not(.disabled) .el-date-table-cell__text) {
  color: #2563eb;
  box-shadow: inset 0 0 0 1px rgba(59, 130, 246, 0.32);
  background: rgba(59, 130, 246, 0.08);
}

:global(.plan-datetime-popper .el-date-table td.selected .el-date-table-cell__text) {
  color: #ffffff;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  box-shadow: 0 10px 18px rgba(59, 130, 246, 0.22);
}

:global(.plan-datetime-popper .el-picker-panel__footer) {
  padding: 10px 14px 14px;
  border-top: 1px solid rgba(148, 163, 184, 0.16);
  background: rgba(248, 250, 252, 0.82);
}

:global(.plan-datetime-popper .el-picker-panel__link-btn) {
  color: #2563eb;
  font-weight: 500;
}

:global(.plan-datetime-popper .el-time-panel) {
  border: none;
  border-radius: 16px;
  overflow: hidden;
  background: linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
  box-shadow: 0 18px 40px rgba(15, 23, 42, 0.14);
}

:global(.plan-datetime-popper .el-time-panel__content) {
  border-top: 1px solid rgba(148, 163, 184, 0.12);
}

:global(.plan-datetime-popper .el-time-spinner__item) {
  color: #475569;
  border-radius: 10px;
}

:global(.plan-datetime-popper .el-time-spinner__item:hover:not(.disabled):not(.active)) {
  color: #1d4ed8;
  background: rgba(59, 130, 246, 0.08);
}

:global(.plan-datetime-popper .el-time-spinner__item.active:not(.disabled)) {
  color: #ffffff;
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

:global(.plan-datetime-popper .el-time-panel__footer) {
  padding: 10px 14px 14px;
  border-top: 1px solid rgba(148, 163, 184, 0.16);
  background: rgba(248, 250, 252, 0.82);
}

:global(.plan-datetime-popper .el-time-panel__btn) {
  color: #2563eb;
  font-weight: 500;
}
</style>
