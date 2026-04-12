<template>
  <el-card class="page-card">
    <template #header>
      <div class="header-row">
        <div class="page-title">用户管理</div>
        <div class="header-actions">
          <el-button
            v-permission="'user:manage'"
            type="danger"
            plain
            :disabled="selectedUserIds.length <= 0"
            @click="handleBatchDelete"
          >
            批量删除
          </el-button>
          <el-button v-permission="'user:manage'" type="primary" @click="openCreateDialog">新增用户</el-button>
          <el-button v-permission="'user:manage'" type="primary" @click="openImportDialog">批量导入</el-button>
        </div>
      </div>
    </template>

    <el-form :inline="true" :model="queryForm" class="query-form">
      <el-form-item label="关键词">
        <el-input
          v-model.trim="queryForm.keyword"
          clearable
          placeholder="姓名 / 学号 / 工号"
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="queryForm.status" clearable placeholder="全部">
          <el-option label="启用" value="ENABLED" />
          <el-option label="禁用" value="DISABLED" />
          <el-option label="待审核" value="PENDING" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>

    <el-table ref="userTableRef" v-loading="loading" :data="tableData" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="48" />
      <el-table-column label="用户标识" width="90">
        <template #default="{ row }">
          <span>{{ row.accountNo || row.studentNo || row.teacherNo || '-' }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="realName" label="姓名" width="140" />
      <el-table-column label="身份" width="110">
        <template #default="{ row }">{{ displayIdentityLabel(row.identityType) }}</template>
      </el-table-column>
      <el-table-column label="角色" min-width="220">
        <template #default="{ row }">
          <div class="role-tag-list">
            <el-tag
              v-for="item in formatRoleNames(row)"
              :key="`${row.id}-${item}`"
              size="small"
              effect="plain"
            >
              {{ item }}
            </el-tag>
            <span v-if="formatRoleNames(row).length === 0">-</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="studentNo" label="学号" width="120" />
      <el-table-column prop="teacherNo" label="工号" width="120" />
      <el-table-column label="登录账号" width="130">
        <template #default="{ row }">{{ row.accountNo || '-' }}</template>
      </el-table-column>
      <el-table-column label="院系/专业/年级" min-width="230">
        <template #default="{ row }">{{ formatOrgInfo(row) }}</template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusTagType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="需改密" width="100">
        <template #default="{ row }">
          <el-tag :type="row.mustChangePassword ? 'warning' : 'success'">{{ row.mustChangePassword ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdTime" label="创建时间" width="180" />
      <el-table-column label="操作" min-width="280" fixed="right">
        <template #default="{ row }">
          <div class="action-wrap">
            <el-button v-permission="'user:manage'" link type="primary" @click="openRoleDialog(row)">
              配置角色
            </el-button>
            <el-button v-permission="'user:manage'" link type="primary" @click="handleToggleStatus(row)">
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button v-permission="'user:manage'" link type="danger" @click="handleResetPassword(row.id)">
              重置密码
            </el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrap">
      <el-pagination
        background
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        :current-page="page"
        :page-size="size"
        :page-sizes="[10, 20, 50, 100]"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog v-model="roleDialogVisible" title="配置账号角色" width="620px" destroy-on-close>
      <div v-if="currentUser" class="role-dialog-user">
        <div class="role-dialog-user__name">{{ currentUser.realName || currentUser.accountNo }}</div>
        <div class="role-dialog-user__meta">登录账号：{{ currentUser.accountNo || '-' }}</div>
      </div>

      <el-alert
        title="为同一账号分配多个角色后，用户登录进入系统可在顶部切换角色查看不同业务范围。"
        type="info"
        :closable="false"
        show-icon
        class="role-dialog-tip"
      />

      <el-checkbox-group v-model="roleForm.roleCodes" class="role-checkbox-group">
        <el-checkbox
          v-for="item in currentUserRoleOptions"
          :key="item.roleCode"
          :label="item.roleCode"
          class="role-checkbox-item"
        >
          {{ item.roleName }} ({{ item.roleCode }})
        </el-checkbox>
      </el-checkbox-group>

      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="roleSaving" @click="handleSaveRoles">保存角色</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="createDialogVisible" title="新增用户" width="680px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px" class="create-form">
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="createForm.realName" placeholder="请输入姓名" clearable />
        </el-form-item>

        <el-form-item label="身份类型" prop="identityType">
          <el-select v-model="createForm.identityType" placeholder="请选择身份类型" style="width: 100%">
            <el-option label="师范生" value="STUDENT" />
            <el-option label="教师" value="TEACHER" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>

        <el-form-item label="学号" prop="studentNo" v-if="createForm.identityType === 'STUDENT'">
          <el-input v-model="createForm.studentNo" placeholder="学生必须填写学号" clearable />
        </el-form-item>

        <el-form-item label="工号" prop="teacherNo" v-else>
          <el-input
            v-model="createForm.teacherNo"
            :placeholder="createForm.identityType === 'ADMIN' ? '管理员必须填写工号' : '教师必须填写工号'"
            clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="createForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>

        <el-form-item label="邮箱" prop="email">
          <el-input v-model="createForm.email" placeholder="请输入邮箱" clearable />
        </el-form-item>

        <el-form-item label="院系" prop="deptId">
          <el-select v-model="createForm.deptId" :placeholder="deptPlaceholder" style="width: 100%" clearable>
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.id"
              :label="dept.name"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="showMajorField" label="专业" prop="majorId">
          <el-select
            v-model="createForm.majorId"
            :placeholder="createForm.deptId ? '请选择专业' : '请先选择院系'"
            style="width: 100%"
            clearable
            :disabled="!createForm.deptId"
          >
            <el-option
              v-for="major in majorOptions"
              :key="major.id"
              :label="major.name"
              :value="major.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="showGradeField" label="年级" prop="gradeId">
          <el-select v-model="createForm.gradeId" placeholder="请选择年级" style="width: 100%" clearable>
            <el-option
              v-for="grade in gradeOptions"
              :key="grade.id"
              :label="grade.name"
              :value="grade.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="初始密码" prop="password">
          <el-input v-model="createForm.password" type="password" placeholder="请输入初始密码（6-32位）" show-password />
        </el-form-item>

        <el-form-item label="角色" prop="roleCodes">
          <el-checkbox-group v-model="createForm.roleCodes" class="role-checkbox-group">
            <el-checkbox
              v-for="role in createRoleOptions"
              :key="role.roleCode"
              :label="role.roleCode"
            >
              {{ role.roleName }} ({{ role.roleCode }})
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="createLoading" @click="handleCreateSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="importDialogVisible" title="用户批量导入" width="680px">
      <el-alert
        title="导入模板列：姓名、身份类型、学号、工号、手机号、院系编码/名称、专业编码/名称、年级编码/名称；登录账号将按学号或工号自动生成。"
        type="info"
        :closable="false"
        show-icon
        class="import-tip"
      />
      <el-upload
        :auto-upload="false"
        :limit="1"
        accept=".xlsx,.xls"
        :on-change="handleImportFileChange"
        :on-remove="handleImportFileRemove"
      >
        <el-button type="primary">选择表格文件</el-button>
      </el-upload>
      <div class="import-actions">
        <el-button type="success" :loading="importLoading" @click="handleImportSubmit">开始导入</el-button>
      </div>
      <div v-if="selectedImportFile" class="selected-file">已选择文件：{{ selectedImportFile.name }}</div>
      <div v-if="importResult" class="import-result">
        <el-descriptions :column="3" border>
          <el-descriptions-item label="总行数">{{ importResult.totalRows }}</el-descriptions-item>
          <el-descriptions-item label="成功">{{ importResult.successRows }}</el-descriptions-item>
          <el-descriptions-item label="失败">{{ importResult.failedRows }}</el-descriptions-item>
        </el-descriptions>
        <el-table v-if="importResult.errors?.length" :data="importResult.errors" border style="margin-top: 12px">
          <el-table-column prop="rowNumber" label="行号" width="100" />
          <el-table-column prop="message" label="错误信息" min-width="280" />
        </el-table>
      </div>
    </el-dialog>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox, type UploadFile, type FormInstance, type FormRules, type TableInstance } from 'element-plus';
import {
  createUserApi,
  batchDeleteUsersApi,
  importUsersApi,
  queryDepartmentsApi,
  queryGradesApi,
  queryMajorsApi,
  queryUsersApi,
  resetUserPasswordApi,
  type CreateUserRequest,
  type UserIdentityType,
  updateUserRolesApi,
  updateUserStatusApi
} from '@/api/modules/admin-users';
import { queryRoleListApi } from '@/api/modules/system-admin';
import { useAuthStore } from '@/store/modules/auth';
import type { SysRoleItem, UserImportResultData, UserItem, IdNameOption, IdValue } from '@/types/api';

const authStore = useAuthStore();
const canManage = computed(() => authStore.permissionCodes.includes('user:manage'));

const loading = ref(false);
const tableData = ref<UserItem[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const userTableRef = ref<TableInstance>();
const selectedUserIds = ref<IdValue[]>([]);

const roleOptions = ref<SysRoleItem[]>([]);
const roleDialogVisible = ref(false);
const roleSaving = ref(false);
const currentUser = ref<UserItem | null>(null);
const roleForm = reactive({
  roleCodes: [] as string[]
});

const createDialogVisible = ref(false);
const createLoading = ref(false);
const createFormRef = ref<FormInstance>();
const createForm = reactive<CreateUserRequest>({
  realName: '',
  identityType: 'STUDENT',
  studentNo: '',
  teacherNo: '',
  phone: '',
  email: '',
  deptId: undefined,
  majorId: undefined,
  gradeId: undefined,
  password: '',
  roleCodes: [] as string[]
});

const departmentOptions = ref<IdNameOption[]>([]);
const majorOptions = ref<IdNameOption[]>([]);
const gradeOptions = ref<IdNameOption[]>([]);

function normalizeUserIdentity(identityType?: string): UserIdentityType {
  if (identityType === 'STUDENT' || identityType === 'TEACHER' || identityType === 'ADMIN') {
    return identityType;
  }
  return 'STUDENT';
}

const createRoleOptions = computed(() => roleOptions.value);
const currentUserRoleOptions = computed(() => roleOptions.value);

const showMajorField = computed(() => createForm.identityType === 'STUDENT');
const showGradeField = computed(() => createForm.identityType === 'STUDENT');
const requiresDeptSelection = computed(() => {
  return createForm.identityType === 'STUDENT'
    || createForm.identityType === 'TEACHER'
    || createForm.roleCodes.includes('DEPT_ADMIN');
});
const deptPlaceholder = computed(() => {
  if (createForm.identityType === 'STUDENT') {
    return '请选择院系';
  }
  if (createForm.identityType === 'TEACHER') {
    return '教师必须选择院系';
  }
  if (createForm.roleCodes.includes('DEPT_ADMIN')) {
    return '院系管理员必须选择院系';
  }
  return '教务处/系统管理员可不选院系';
});

function validateStudentNo(_: unknown, value: string, callback: (error?: Error) => void) {
  if (createForm.identityType === 'STUDENT' && !value?.trim()) {
    callback(new Error('学生必须填写学号'));
    return;
  }
  callback();
}

function validateTeacherNo(_: unknown, value: string, callback: (error?: Error) => void) {
  if (createForm.identityType !== 'STUDENT' && !value?.trim()) {
    callback(new Error(createForm.identityType === 'ADMIN' ? '管理员必须填写工号' : '教师必须填写工号'));
    return;
  }
  callback();
}

function validateStudentField(
  value: IdValue | undefined,
  callback: (error?: Error) => void,
  fieldLabel: string
) {
  if (createForm.identityType === 'STUDENT' && value == null) {
    callback(new Error(`学生必须选择${fieldLabel}`));
    return;
  }
  callback();
}

function validateDeptField(_: unknown, value: IdValue | undefined, callback: (error?: Error) => void) {
  if (requiresDeptSelection.value && value == null) {
    callback(new Error(createForm.identityType === 'ADMIN' ? '院系管理员必须选择院系' : '请先选择院系'));
    return;
  }
  callback();
}

function validateRoleCodes(_: unknown, value: string[], callback: (error?: Error) => void) {
  if (!value?.length) {
    callback(new Error('请至少选择一个角色'));
    return;
  }
  callback();
}

const createRules: FormRules = {
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  identityType: [
    { required: true, message: '请选择身份类型', trigger: 'change' }
  ],
  studentNo: [
    { validator: validateStudentNo, trigger: 'blur' }
  ],
  teacherNo: [
    { validator: validateTeacherNo, trigger: 'blur' }
  ],
  deptId: [
    {
      validator: validateDeptField,
      trigger: 'change'
    }
  ],
  majorId: [
    {
      validator: (_rule, value, callback) => validateStudentField(value, callback, '专业'),
      trigger: 'change'
    }
  ],
  gradeId: [
    {
      validator: (_rule, value, callback) => validateStudentField(value, callback, '年级'),
      trigger: 'change'
    }
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度必须在6-32位之间', trigger: 'blur' }
  ],
  roleCodes: [
    { validator: validateRoleCodes, trigger: 'change' }
  ]
};

const importDialogVisible = ref(false);
const importLoading = ref(false);
const selectedImportFile = ref<File | null>(null);
const importResult = ref<UserImportResultData | null>(null);

const queryForm = reactive({
  keyword: '',
  status: '' as '' | 'ENABLED' | 'DISABLED' | 'PENDING'
});

function identityLabel(identityType: string) {
  if (identityType === 'STUDENT') return '师范生';
  if (identityType === 'TEACHER') return '教师';
  if (identityType === 'ADMIN') return '管理员';
  return identityType || '-';
}

function displayIdentityLabel(identityType: string) {
  const normalizedIdentity = normalizeUserIdentity(identityType);
  if (normalizedIdentity === 'STUDENT') return '师范生';
  if (normalizedIdentity === 'TEACHER') return '教师';
  if (normalizedIdentity === 'ADMIN') return '管理员';
  return identityType || '-';
}

function formatDisplayUserId(userId: IdValue | undefined) {
  if (userId == null) {
    return '-';
  }
  const rawValue = String(userId).trim();
  if (!rawValue) {
    return '-';
  }
  if (rawValue.length <= 4) {
    return rawValue;
  }
  return rawValue.slice(-4);
}

function statusLabel(status: string) {
  if (status === 'ENABLED') return '启用';
  if (status === 'DISABLED') return '禁用';
  if (status === 'PENDING') return '待审核';
  return status || '-';
}

function statusTagType(status: string) {
  if (status === 'ENABLED') return 'success';
  if (status === 'DISABLED') return 'info';
  if (status === 'PENDING') return 'warning';
  return 'info';
}

function formatOrgInfo(row: UserItem) {
  const parts = [row.deptName, row.majorName, row.gradeName].filter(Boolean);
  return parts.length ? parts.join(' / ') : '-';
}

function formatRoleNames(row: UserItem) {
  if (row.roleCodes?.length) {
    return row.roleCodes.map((code) => {
      const matched = roleOptions.value.find((item) => item.roleCode === code);
      return matched?.roleName || code;
    });
  }
  if (row.roleNames) {
    return row.roleNames
      .split(/[、,，/]/)
      .map((item) => item.trim())
      .filter(Boolean);
  }
  return [];
}

async function fetchRoleOptions() {
  const resp = await queryRoleListApi();
  roleOptions.value = (resp.data || []).filter((item) => item.status === 'ENABLED');
}

async function fetchData() {
  loading.value = true;
  try {
    const resp = await queryUsersApi({
      page: page.value,
      size: size.value,
      keyword: queryForm.keyword || undefined,
      status: queryForm.status || undefined
    });
    tableData.value = resp.data.records || [];
    total.value = Number(resp.data.total || 0);
    selectedUserIds.value = [];
    userTableRef.value?.clearSelection();
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  page.value = 1;
  fetchData();
}

function handleReset() {
  queryForm.keyword = '';
  queryForm.status = '';
  page.value = 1;
  fetchData();
}

function handleSizeChange(nextSize: number) {
  size.value = nextSize;
  page.value = 1;
  fetchData();
}

function handleCurrentChange(nextPage: number) {
  page.value = nextPage;
  fetchData();
}

function handleSelectionChange(rows: UserItem[]) {
  selectedUserIds.value = rows.map((row) => row.id);
}

function openRoleDialog(row: UserItem) {
  currentUser.value = row;
  roleForm.roleCodes = [...(row.roleCodes || [])];
  roleDialogVisible.value = true;
}

async function handleSaveRoles() {
  if (!currentUser.value) {
    return;
  }
  if (!roleForm.roleCodes.length) {
    ElMessage.warning('请至少选择一个角色');
    return;
  }
  roleSaving.value = true;
  try {
    await updateUserRolesApi(currentUser.value.id, {
      roleCodes: roleForm.roleCodes
    });
    ElMessage.success('角色配置已更新');
    roleDialogVisible.value = false;
    await fetchData();
  } finally {
    roleSaving.value = false;
  }
}

async function handleToggleStatus(row: UserItem) {
  if (!canManage.value) return;
  const targetStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED';
  await ElMessageBox.confirm(`确认将该用户状态修改为“${statusLabel(targetStatus)}”吗？`, '提示', { type: 'warning' });
  await updateUserStatusApi(row.id, { status: targetStatus });
  ElMessage.success('用户状态更新成功');
  fetchData();
}

async function handleResetPassword(userId: string) {
  if (!canManage.value) return;
  await ElMessageBox.confirm('确认将该用户密码重置为默认初始密码吗？', '提示', { type: 'warning' });
  await resetUserPasswordApi(userId);
  ElMessage.success('密码已重置为默认初始密码');
  fetchData();
}

async function handleBatchDelete() {
  if (!canManage.value || selectedUserIds.value.length <= 0) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      `确认删除选中的 ${selectedUserIds.value.length} 个用户吗？仅未参与业务流程的账号允许删除。`,
      '批量删除确认',
      { type: 'warning', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    );
  } catch {
    return;
  }
  await batchDeleteUsersApi(selectedUserIds.value);
  ElMessage.success('用户批量删除成功');
  selectedUserIds.value = [];
  userTableRef.value?.clearSelection();
  fetchData();
}

function openImportDialog() {
  selectedImportFile.value = null;
  importResult.value = null;
  importDialogVisible.value = true;
}

function handleImportFileChange(file: UploadFile) {
  selectedImportFile.value = file.raw || null;
}

function handleImportFileRemove() {
  selectedImportFile.value = null;
}

async function handleImportSubmit() {
  if (!selectedImportFile.value) {
    ElMessage.warning('请先选择表格文件');
    return;
  }
  importLoading.value = true;
  try {
    const resp = await importUsersApi(selectedImportFile.value);
    importResult.value = resp.data;
    ElMessage.success('导入完成');
    fetchData();
  } finally {
    importLoading.value = false;
  }
}

async function fetchDepartmentOptions() {
  try {
    const resp = await queryDepartmentsApi();
    departmentOptions.value = resp.data || [];
  } catch (error) {
    console.error('获取院系列表失败:', error);
  }
}

async function fetchMajorOptions(deptId?: IdValue) {
  if (deptId == null) {
    majorOptions.value = [];
    return;
  }
  try {
    const resp = await queryMajorsApi(deptId);
    majorOptions.value = resp.data || [];
  } catch (error) {
    console.error('获取专业列表失败:', error);
  }
}

async function fetchGradeOptions() {
  try {
    const resp = await queryGradesApi();
    gradeOptions.value = resp.data || [];
  } catch (error) {
    console.error('获取年级列表失败:', error);
  }
}

function trimOptionalText(value?: string) {
  const trimmed = value?.trim();
  return trimmed ? trimmed : undefined;
}

function normalizeCreateFormByIdentity(identityType: UserIdentityType) {
  if (identityType === 'STUDENT') {
    createForm.teacherNo = '';
    return;
  }

  createForm.studentNo = '';
  createForm.majorId = undefined;
  createForm.gradeId = undefined;
  majorOptions.value = [];
}

function buildCreatePayload(): CreateUserRequest {
  const isStudent = createForm.identityType === 'STUDENT';
  const shouldSubmitDept = isStudent
    || createForm.identityType === 'TEACHER'
    || createForm.roleCodes.includes('DEPT_ADMIN');

  return {
    realName: createForm.realName.trim(),
    identityType: createForm.identityType,
    studentNo: isStudent ? trimOptionalText(createForm.studentNo) : undefined,
    teacherNo: isStudent ? undefined : trimOptionalText(createForm.teacherNo),
    phone: trimOptionalText(createForm.phone),
    email: trimOptionalText(createForm.email),
    deptId: shouldSubmitDept ? createForm.deptId : undefined,
    majorId: isStudent ? createForm.majorId : undefined,
    gradeId: isStudent ? createForm.gradeId : undefined,
    password: createForm.password,
    roleCodes: [...createForm.roleCodes]
  };
}

async function openCreateDialog() {
  createForm.realName = '';
  createForm.identityType = 'STUDENT';
  createForm.studentNo = '';
  createForm.teacherNo = '';
  createForm.phone = '';
  createForm.email = '';
  createForm.deptId = undefined;
  createForm.majorId = undefined;
  createForm.gradeId = undefined;
  createForm.password = '';
  createForm.roleCodes = [];
  majorOptions.value = [];
  createFormRef.value?.clearValidate();
  await Promise.all([fetchDepartmentOptions(), fetchGradeOptions()]);
  createDialogVisible.value = true;
}

async function handleCreateSubmit() {
  if (!createFormRef.value) return;
  const valid = await createFormRef.value.validate().catch(() => false);
  if (!valid) return;

  createLoading.value = true;
  try {
    await createUserApi(buildCreatePayload());
    ElMessage.success('用户创建成功');
    createDialogVisible.value = false;
    await fetchData();
  } finally {
    createLoading.value = false;
  }
}

watch(
  () => createForm.identityType,
  async (identityType) => {
    normalizeCreateFormByIdentity(identityType);

    if (identityType === 'STUDENT' && createForm.deptId != null) {
      await fetchMajorOptions(createForm.deptId);
    }

    createFormRef.value?.clearValidate(['studentNo', 'teacherNo', 'deptId', 'majorId', 'gradeId', 'roleCodes']);
  }
);

watch(
  () => [...createForm.roleCodes],
  () => {
    createFormRef.value?.clearValidate(['deptId', 'roleCodes']);
  }
);

watch(
  () => createForm.deptId,
  async (deptId) => {
    createForm.majorId = undefined;
    if (showMajorField.value && deptId != null) {
      await fetchMajorOptions(deptId);
    } else {
      majorOptions.value = [];
    }
    createFormRef.value?.clearValidate(['deptId', 'majorId']);
  }
);

onMounted(async () => {
  await Promise.all([fetchData(), fetchRoleOptions(), fetchDepartmentOptions(), fetchGradeOptions()]);
});
</script>

<style scoped lang="scss">
.page-card {
  min-height: 100%;
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

.page-title {
  font-size: 20px;
  font-weight: 600;
}

.query-form {
  margin-bottom: 16px;
}

.role-tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pagination-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.action-wrap {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.role-dialog-user {
  margin-bottom: 12px;
}

.role-dialog-user__name {
  font-size: 16px;
  font-weight: 600;
  color: #1f2937;
}

.role-dialog-user__meta {
  margin-top: 4px;
  color: #6b7280;
  font-size: 13px;
}

.role-dialog-tip {
  margin-bottom: 16px;
}

.role-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.role-checkbox-item {
  margin-right: 0;
}

.import-tip {
  margin-bottom: 12px;
}

.import-actions {
  margin-top: 12px;
}

.selected-file {
  margin-top: 12px;
  color: #606266;
}

.import-result {
  margin-top: 16px;
}
</style>
