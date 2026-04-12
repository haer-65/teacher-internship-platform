<template>
  <div class="assignment-page">
    <template v-if="isAdminRole">
      <el-card class="card-block">
        <template #header>
          <div class="header-row">
            <div class="title">待分配申请</div>
            <div class="header-actions">
              <el-button @click="handleBack">返回</el-button>
            </div>
          </div>
        </template>

        <el-form :inline="true" :model="candidateQuery" class="query-form">
          <el-form-item v-if="fixedPlanIdText" label="当前计划">
            <el-tag type="info">{{ fixedPlanIdText }}</el-tag>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model.trim="candidateQuery.keyword" clearable placeholder="计划名称/个人陈述" />
          </el-form-item>
          <el-form-item label="学号">
            <el-input v-model.trim="candidateQuery.studentNo" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleCandidateSearch">查询</el-button>
            <el-button @click="handleCandidateReset">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="candidateLoading" :data="candidateData" border>
          <el-table-column prop="applicationId" label="申请编号" width="120" />
          <el-table-column prop="planCode" label="计划编号" width="150" />
          <el-table-column prop="planName" label="计划名称" min-width="190" />
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column label="院系/专业/年级" min-width="220">
            <template #default="{ row }">{{ formatStudentOrgInfo(row) }}</template>
          </el-table-column>
          <el-table-column label="实习基地志愿" min-width="240">
            <template #default="{ row }">{{ formatPreferredBases(row.preferredBases) }}</template>
          </el-table-column>
          <el-table-column prop="submittedTime" label="提交时间" width="170" />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button
                v-permission="'application:assign'"
                link
                type="primary"
                :disabled="row.planStatus !== 'PUBLISHED'"
                @click="openAssignDialog(row)"
              >
                手动分配
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager-wrap">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="candidateTotal"
            :current-page="candidateQuery.page"
            :page-size="candidateQuery.size"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleCandidateSizeChange"
            @current-change="handleCandidateCurrentChange"
          />
        </div>
      </el-card>

      <el-card v-if="showAssignmentRecordPanel" class="card-block">
        <template #header>
          <div class="header-row">
            <div class="title">分配记录</div>
          </div>
        </template>

        <el-form :inline="true" :model="assignmentQuery" class="query-form">
          <el-form-item v-if="fixedPlanIdText" label="当前计划">
            <el-tag type="info">{{ fixedPlanIdText }}</el-tag>
          </el-form-item>
          <el-form-item label="关键词">
            <el-input v-model.trim="assignmentQuery.keyword" clearable placeholder="计划名称/学生姓名/调整原因" />
          </el-form-item>
          <el-form-item label="学号">
            <el-input v-model.trim="assignmentQuery.studentNo" clearable />
          </el-form-item>
          <el-form-item label="是否当前版本">
            <el-select v-model="assignmentQuery.isCurrent" clearable placeholder="全部">
              <el-option label="是" :value="1" />
              <el-option label="否" :value="0" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleAssignmentSearch">查询</el-button>
            <el-button @click="handleAssignmentReset">重置</el-button>
          </el-form-item>
        </el-form>

        <el-table v-loading="assignmentLoading" :data="assignmentData" border>
          <el-table-column prop="id" label="分配编号" width="110" />
          <el-table-column prop="planCode" label="计划编号" width="150" />
          <el-table-column prop="studentNo" label="学号" width="120" />
          <el-table-column prop="studentName" label="姓名" width="120" />
          <el-table-column label="院系/专业/年级" min-width="220">
            <template #default="{ row }">{{ formatStudentOrgInfo(row) }}</template>
          </el-table-column>
          <el-table-column prop="baseName" label="实习基地" min-width="160" />
          <el-table-column prop="innerTeacherName" label="校内指导教师" width="150" />
          <el-table-column prop="baseTeacherName" label="基地指导教师" width="150" />
          <el-table-column prop="versionNo" label="版本号" width="90" />
          <el-table-column prop="isCurrent" label="当前版本" width="90">
            <template #default="{ row }">
              <el-tag :type="row.isCurrent === 1 ? 'success' : 'info'">{{ row.isCurrent === 1 ? '是' : '否' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="adjustReason" label="调整原因" min-width="180" />
          <el-table-column prop="assignedTime" label="分配时间" width="170" />
          <el-table-column label="操作" width="130" fixed="right">
            <template #default="{ row }">
              <el-button
                v-permission="'application:assign'"
                link
                type="warning"
                :disabled="row.isCurrent !== 1 || row.planStatus !== 'PUBLISHED'"
                @click="openAdjustDialog(row)"
              >
                调整
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pager-wrap">
          <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="assignmentTotal"
            :current-page="assignmentQuery.page"
            :page-size="assignmentQuery.size"
            :page-sizes="[10, 20, 50, 100]"
            @size-change="handleAssignmentSizeChange"
            @current-change="handleAssignmentCurrentChange"
          />
        </div>
      </el-card>
    </template>

    <el-card v-else>
      <template #header>
        <div class="header-row">
          <div class="title">我的分配</div>
        </div>
      </template>

      <el-form :inline="true" :model="myQuery" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model.trim="myQuery.keyword" clearable placeholder="计划名称/调整原因" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleMySearch">查询</el-button>
          <el-button @click="handleMyReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="assignmentLoading" :data="assignmentData" border>
        <el-table-column prop="planCode" label="计划编号" width="150" />
        <el-table-column prop="planName" label="计划名称" min-width="200" />
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="studentName" label="姓名" width="120" />
        <el-table-column label="院系/专业/年级" min-width="220">
          <template #default="{ row }">{{ formatStudentOrgInfo(row) }}</template>
        </el-table-column>
        <el-table-column prop="baseName" label="实习基地" min-width="160" />
        <el-table-column prop="innerTeacherName" label="校内指导教师" width="150" />
        <el-table-column prop="baseTeacherName" label="基地指导教师" width="150" />
        <el-table-column prop="versionNo" label="版本号" width="90" />
        <el-table-column prop="assignedTime" label="分配时间" width="170" />
        <el-table-column prop="adjustReason" label="调整原因" min-width="180" />
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="assignmentTotal"
          :current-page="myQuery.page"
          :page-size="myQuery.size"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="handleMySizeChange"
          @current-change="handleMyCurrentChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="assignDialogVisible" title="手动分配" width="640px">
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="120px">
        <el-form-item label="申请编号" prop="applicationId">
          <el-input :model-value="String(assignForm.applicationId || '')" disabled />
        </el-form-item>
        <el-form-item label="实习基地" prop="baseId">
          <el-select v-model="assignForm.baseId" filterable style="width: 100%">
            <el-option v-for="item in optionData.internshipBases" :key="String(item.id)" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="校内指导教师" prop="innerTeacherId">
          <el-select v-model="assignForm.innerTeacherId" filterable style="width: 100%">
            <el-option v-for="item in optionData.innerTeachers" :key="String(item.id)" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="基地指导教师" prop="baseTeacherId">
          <el-select v-model="assignForm.baseTeacherId" filterable style="width: 100%">
            <el-option v-for="item in optionData.baseTeachers" :key="String(item.id)" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model.trim="assignForm.adjustReason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignSaving" @click="handleManualAssign">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="adjustDialogVisible" title="调整分配" width="640px">
      <el-form ref="adjustFormRef" :model="adjustForm" :rules="adjustRules" label-width="120px">
        <el-form-item label="实习基地" prop="baseId">
          <el-select v-model="adjustForm.baseId" filterable style="width: 100%">
            <el-option v-for="item in optionData.internshipBases" :key="String(item.id)" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="校内指导教师" prop="innerTeacherId">
          <el-select v-model="adjustForm.innerTeacherId" filterable style="width: 100%">
            <el-option v-for="item in optionData.innerTeachers" :key="String(item.id)" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="基地指导教师" prop="baseTeacherId">
          <el-select v-model="adjustForm.baseTeacherId" filterable style="width: 100%">
            <el-option v-for="item in optionData.baseTeachers" :key="String(item.id)" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="调整原因" prop="adjustReason">
          <el-input v-model.trim="adjustForm.adjustReason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="adjustDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="adjustSaving" @click="handleAdjustAssign">保存</el-button>
      </template>
    </el-dialog>

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, type FormInstance, type FormRules } from 'element-plus';
import { useAuthStore } from '@/store/modules/auth';
import {
  adjustAssignmentApi,
  getAssignmentDetailApi,
  manualAssignApi,
  queryAssignmentCandidatePageApi,
  queryAssignmentOptionsApi,
  queryAssignmentPageApi,
  queryMyAssignmentPageApi
} from '@/api/modules/application';
import type {
  ApplicationPreferenceItem,
  AssignmentCandidateItem,
  AssignmentItem,
  AssignmentOptionData,
  IdValue
} from '@/types/api';

const route = useRoute();
const router = useRouter();
const authStore = useAuthStore();
const isAdminRole = authStore.currentRoleCode === 'DEPT_ADMIN' || authStore.currentRoleCode === 'SYS_ADMIN';

function normalizeQueryId(value: unknown): string | undefined {
  const rawValue = Array.isArray(value) ? value[0] : value;
  if (rawValue === undefined || rawValue === null || rawValue === '') {
    return undefined;
  }
  return String(rawValue);
}

const fixedPlanId = computed<string | undefined>(() => normalizeQueryId(route.query.planId));
const queryApplicationId = computed<string | undefined>(() => normalizeQueryId(route.query.applicationId));
const queryAssignmentId = computed<string | undefined>(() => normalizeQueryId(route.query.assignmentId));
const fixedPlanIdText = computed(() => (fixedPlanId.value ? `计划ID：${fixedPlanId.value}` : ''));
const showAssignmentRecordPanel = computed(() => !queryApplicationId.value && !queryAssignmentId.value);

const candidateLoading = ref(false);
const assignmentLoading = ref(false);
const candidateData = ref<AssignmentCandidateItem[]>([]);
const assignmentData = ref<AssignmentItem[]>([]);
const candidateTotal = ref(0);
const assignmentTotal = ref(0);
const optionData = reactive<AssignmentOptionData>({
  internshipBases: [],
  innerTeachers: [],
  baseTeachers: []
});

const candidateQuery = reactive({
  page: 1,
  size: 10,
  keyword: '',
  studentNo: '',
  planId: undefined as IdValue | undefined
});

const assignmentQuery = reactive({
  page: 1,
  size: 10,
  keyword: '',
  studentNo: '',
  isCurrent: 1 as number | undefined,
  planId: undefined as IdValue | undefined
});

const myQuery = reactive({
  page: 1,
  size: 10,
  keyword: ''
});

const assignDialogVisible = ref(false);
const adjustDialogVisible = ref(false);
const assignSaving = ref(false);
const adjustSaving = ref(false);
const currentAdjustAssignmentId = ref<IdValue>('');

const assignFormRef = ref<FormInstance>();
const adjustFormRef = ref<FormInstance>();

const assignForm = reactive({
  applicationId: '' as IdValue,
  baseId: '' as IdValue,
  innerTeacherId: '' as IdValue,
  baseTeacherId: '' as IdValue,
  adjustReason: ''
});

const adjustForm = reactive({
  baseId: '' as IdValue,
  innerTeacherId: '' as IdValue,
  baseTeacherId: '' as IdValue,
  adjustReason: ''
});

const assignRules: FormRules = {
  applicationId: [{ required: true, message: '申请编号不能为空', trigger: 'change' }],
  baseId: [{ required: true, message: '请选择实习基地', trigger: 'change' }],
  innerTeacherId: [{ required: true, message: '请选择校内指导教师', trigger: 'change' }],
  baseTeacherId: [{ required: true, message: '请选择基地指导教师', trigger: 'change' }]
};

const adjustRules: FormRules = {
  baseId: [{ required: true, message: '请选择实习基地', trigger: 'change' }],
  innerTeacherId: [{ required: true, message: '请选择校内指导教师', trigger: 'change' }],
  baseTeacherId: [{ required: true, message: '请选择基地指导教师', trigger: 'change' }],
  adjustReason: [{ required: true, message: '请填写调整原因', trigger: 'blur' }]
};

function formatStudentOrgInfo(row: {
  studentDeptName?: string;
  studentMajorName?: string;
  studentGradeName?: string;
}) {
  const parts = [row.studentDeptName, row.studentMajorName, row.studentGradeName].filter(Boolean);
  return parts.length ? parts.join(' / ') : '-';
}

function formatPreferredBases(preferredBases: ApplicationPreferenceItem[] = []) {
  if (!preferredBases.length) {
    return '-';
  }
  return preferredBases
    .map((item) => `第${item.preferenceOrder}志愿：${item.baseName || item.baseCode || '-'}`)
    .join('；');
}

function applyRoutePlanId() {
  candidateQuery.planId = fixedPlanId.value;
  assignmentQuery.planId = fixedPlanId.value;
}

async function fetchOptionData(planId?: IdValue) {
  if (!isAdminRole) {
    return;
  }
  const resp = await queryAssignmentOptionsApi(planId || undefined);
  optionData.internshipBases = resp.data.internshipBases || [];
  optionData.innerTeachers = resp.data.innerTeachers || [];
  optionData.baseTeachers = resp.data.baseTeachers || [];
}

async function fetchCandidateData() {
  if (!isAdminRole) {
    return;
  }
  candidateLoading.value = true;
  try {
    const resp = await queryAssignmentCandidatePageApi({
      page: candidateQuery.page,
      size: candidateQuery.size,
      keyword: candidateQuery.keyword || undefined,
      studentNo: candidateQuery.studentNo || undefined,
      planId: candidateQuery.planId || undefined
    });
    candidateData.value = resp.data.records || [];
    candidateTotal.value = Number(resp.data.total || 0);
  } finally {
    candidateLoading.value = false;
  }
}

async function fetchAssignmentData() {
  assignmentLoading.value = true;
  try {
    if (isAdminRole) {
      const resp = await queryAssignmentPageApi({
        page: assignmentQuery.page,
        size: assignmentQuery.size,
        keyword: assignmentQuery.keyword || undefined,
        studentNo: assignmentQuery.studentNo || undefined,
        isCurrent: assignmentQuery.isCurrent,
        planId: assignmentQuery.planId || undefined
      });
      assignmentData.value = resp.data.records || [];
      assignmentTotal.value = Number(resp.data.total || 0);
      return;
    }

    const resp = await queryMyAssignmentPageApi({
      page: myQuery.page,
      size: myQuery.size,
      keyword: myQuery.keyword || undefined
    });
    assignmentData.value = resp.data.records || [];
    assignmentTotal.value = Number(resp.data.total || 0);
  } finally {
    assignmentLoading.value = false;
  }
}

function handleCandidateSearch() {
  candidateQuery.page = 1;
  fetchCandidateData();
}

function handleCandidateReset() {
  candidateQuery.keyword = '';
  candidateQuery.studentNo = '';
  candidateQuery.page = 1;
  applyRoutePlanId();
  fetchCandidateData();
}

function handleAssignmentSearch() {
  assignmentQuery.page = 1;
  fetchAssignmentData();
}

function handleAssignmentReset() {
  assignmentQuery.keyword = '';
  assignmentQuery.studentNo = '';
  assignmentQuery.isCurrent = 1;
  assignmentQuery.page = 1;
  applyRoutePlanId();
  fetchAssignmentData();
}

function handleMySearch() {
  myQuery.page = 1;
  fetchAssignmentData();
}

function handleMyReset() {
  myQuery.keyword = '';
  myQuery.page = 1;
  fetchAssignmentData();
}

function handleCandidateSizeChange(nextSize: number) {
  candidateQuery.size = nextSize;
  candidateQuery.page = 1;
  fetchCandidateData();
}

function handleCandidateCurrentChange(nextPage: number) {
  candidateQuery.page = nextPage;
  fetchCandidateData();
}

function handleAssignmentSizeChange(nextSize: number) {
  assignmentQuery.size = nextSize;
  assignmentQuery.page = 1;
  fetchAssignmentData();
}

function handleAssignmentCurrentChange(nextPage: number) {
  assignmentQuery.page = nextPage;
  fetchAssignmentData();
}

function handleMySizeChange(nextSize: number) {
  myQuery.size = nextSize;
  myQuery.page = 1;
  fetchAssignmentData();
}

function handleMyCurrentChange(nextPage: number) {
  myQuery.page = nextPage;
  fetchAssignmentData();
}

async function openAssignDialog(row: AssignmentCandidateItem) {
  if (row.planStatus !== 'PUBLISHED') {
    ElMessage.warning('计划已结束，不能再分配或调整');
    return;
  }
  await fetchOptionData(row.planId);
  assignForm.applicationId = row.applicationId;
  assignForm.baseId = row.preferredBases?.[0]?.baseId || optionData.internshipBases[0]?.id || '';
  assignForm.innerTeacherId = '';
  assignForm.baseTeacherId = '';
  assignForm.adjustReason = '';
  assignDialogVisible.value = true;
  assignFormRef.value?.clearValidate();
}

async function openAdjustDialog(row: AssignmentItem) {
  if (row.planStatus !== 'PUBLISHED') {
    ElMessage.warning('计划已结束，不能再分配或调整');
    return;
  }
  currentAdjustAssignmentId.value = row.id;
  await fetchOptionData(row.planId);
  adjustForm.baseId = row.baseId;
  adjustForm.innerTeacherId = row.innerTeacherId;
  adjustForm.baseTeacherId = row.baseTeacherId;
  adjustForm.adjustReason = '';
  adjustDialogVisible.value = true;
  adjustFormRef.value?.clearValidate();
}

function handleBack() {
  if (window.history.length > 1) {
    router.back();
    return;
  }
  router.push('/application/admin');
}

async function handleManualAssign() {
  const valid = await assignFormRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }
  assignSaving.value = true;
  try {
    await manualAssignApi(assignForm);
    ElMessage.success('分配已保存');
    assignDialogVisible.value = false;
    await Promise.all([fetchCandidateData(), fetchAssignmentData()]);
  } finally {
    assignSaving.value = false;
  }
}

async function handleAdjustAssign() {
  const valid = await adjustFormRef.value?.validate().catch(() => false);
  if (!valid) {
    return;
  }
  adjustSaving.value = true;
  try {
    await adjustAssignmentApi(currentAdjustAssignmentId.value, adjustForm);
    ElMessage.success('分配已调整');
    adjustDialogVisible.value = false;
    await Promise.all([fetchCandidateData(), fetchAssignmentData()]);
  } finally {
    adjustSaving.value = false;
  }
}

async function openAssignDialogFromQuery() {
  if (queryAssignmentId.value) {
    const currentRow = assignmentData.value.find((item) => String(item.id) === queryAssignmentId.value);
    if (currentRow) {
      await openAdjustDialog(currentRow);
      return;
    }

    const resp = await getAssignmentDetailApi(queryAssignmentId.value);
    await openAdjustDialog(resp.data);
    return;
  }

  if (!queryApplicationId.value) {
    return;
  }
  const candidateRow = candidateData.value.find((item) => String(item.applicationId) === queryApplicationId.value);
  if (candidateRow) {
    await openAssignDialog(candidateRow);
    return;
  }

  const assignmentRow = assignmentData.value.find((item) => String(item.applicationId) === queryApplicationId.value);
  if (assignmentRow) {
    await openAdjustDialog(assignmentRow);
    return;
  }
}

async function initializeAdminPage() {
  applyRoutePlanId();
  candidateQuery.page = 1;
  assignmentQuery.page = 1;
  await fetchOptionData(fixedPlanId.value);
  await Promise.all([fetchCandidateData(), fetchAssignmentData()]);
  await openAssignDialogFromQuery();
}

watch(
  () => [route.query.planId, route.query.applicationId, route.query.assignmentId],
  async () => {
    if (!isAdminRole) {
      return;
    }
    assignDialogVisible.value = false;
    await initializeAdminPage();
  }
);

onMounted(async () => {
  if (isAdminRole) {
    await initializeAdminPage();
    return;
  }
  await fetchAssignmentData();
});
</script>

<style scoped lang="scss">
.assignment-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.card-block {
  min-height: 200px;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.title {
  font-size: 20px;
  font-weight: 600;
}

.query-form {
  margin-bottom: 16px;
}

.pager-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
