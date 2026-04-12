<template>
  <el-tabs v-model="activeTab">
    <el-tab-pane label="操作日志" name="operation">
      <el-form :inline="true" class="query-form">
        <el-form-item label="关键词">
          <el-input
            v-model.trim="operationQuery.keyword"
            clearable
            placeholder="操作人/请求地址/业务标识"
            @keyup.enter="searchOperation"
          />
        </el-form-item>
        <el-form-item label="模块">
          <el-select v-model="operationQuery.moduleCode" clearable filterable placeholder="全部">
            <el-option v-for="item in moduleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="动作">
          <el-select v-model="operationQuery.actionCode" clearable filterable placeholder="全部">
            <el-option v-for="item in actionOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="operationQuery.operationStatus" clearable placeholder="全部">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAIL" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作时间">
          <el-date-picker
            v-model="operationRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            range-separator="至"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchOperation">查询</el-button>
          <el-button @click="resetOperation">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="operationPage.loading" :data="operationPage.records" border>
        <el-table-column prop="operateTime" label="操作时间" min-width="170" />
        <el-table-column prop="operatorName" label="操作人" min-width="120">
          <template #default="{ row }">{{ operatorNameLabel(row.operatorName) }}</template>
        </el-table-column>
        <el-table-column prop="moduleCode" label="模块" min-width="120">
          <template #default="{ row }">{{ moduleLabel(row.moduleCode) }}</template>
        </el-table-column>
        <el-table-column prop="actionCode" label="动作" min-width="150">
          <template #default="{ row }">{{ actionLabel(row.actionCode) }}</template>
        </el-table-column>
        <el-table-column prop="requestMethod" label="方法" width="90" />
        <el-table-column prop="requestUri" label="请求地址" min-width="220" show-overflow-tooltip />
        <el-table-column prop="requestIp" label="IP地址" min-width="130" />
        <el-table-column prop="operationStatus" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.operationStatus === 'SUCCESS' ? 'success' : 'danger'">
              {{ operationStatusLabel(row.operationStatus) }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="operationPage.total"
          :current-page="operationPage.page"
          :page-size="operationPage.size"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="onOperationSizeChange"
          @current-change="onOperationCurrentChange"
        />
      </div>
    </el-tab-pane>

    <el-tab-pane label="登录日志" name="login">
      <el-form :inline="true" class="query-form">
        <el-form-item label="关键词">
          <el-input
            v-model.trim="loginQuery.keyword"
            clearable
            placeholder="学号/工号/IP地址"
            @keyup.enter="searchLogin"
          />
        </el-form-item>
        <el-form-item label="结果">
          <el-select v-model="loginQuery.loginResult" clearable placeholder="全部">
            <el-option label="成功" value="SUCCESS" />
            <el-option label="失败" value="FAIL" />
          </el-select>
        </el-form-item>
        <el-form-item label="登录时间">
          <el-date-picker
            v-model="loginRange"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            range-separator="至"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchLogin">查询</el-button>
          <el-button @click="resetLogin">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loginPage.loading" :data="loginPage.records" border>
        <el-table-column prop="loginTime" label="登录时间" min-width="170" />
        <el-table-column prop="accountNo" label="学号/工号" min-width="140" />
        <el-table-column prop="userId" label="用户标识" width="100" />
        <el-table-column prop="loginIp" label="IP地址" min-width="130" />
        <el-table-column prop="userAgent" label="客户端信息" min-width="240" show-overflow-tooltip />
        <el-table-column prop="loginResult" label="结果" width="100">
          <template #default="{ row }">
            <el-tag :type="row.loginResult === 'SUCCESS' ? 'success' : 'danger'">{{ loginResultLabel(row.loginResult) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="failReason" label="失败原因" min-width="180" show-overflow-tooltip />
      </el-table>

      <div class="pager">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="loginPage.total"
          :current-page="loginPage.page"
          :page-size="loginPage.size"
          :page-sizes="[10, 20, 50, 100]"
          @size-change="onLoginSizeChange"
          @current-change="onLoginCurrentChange"
        />
      </div>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { queryLoginLogPageApi, queryOperationLogPageApi } from '@/api/modules/system-admin';
import type { SysLoginLogItem, SysOperationLogItem } from '@/types/api';

type TabType = 'operation' | 'login';

interface PageState<T> {
  loading: boolean;
  page: number;
  size: number;
  total: number;
  records: T[];
}

const activeTab = ref<TabType>('operation');

const moduleOptions = [
  { label: '用户管理', value: 'USER' },
  { label: '基础数据', value: 'BASE' },
  { label: '系统参数', value: 'PARAM' },
  { label: '角色权限', value: 'RBAC' },
  { label: '成绩管理', value: 'SCORE' },
  { label: '系统', value: 'SYSTEM' }
];

const actionOptions = [
  { label: '修改用户状态', value: 'STATUS_UPDATE' },
  { label: '更新用户角色', value: 'ROLE_UPDATE' },
  { label: '重置密码', value: 'RESET_PASSWORD' },
  { label: '批量导入用户', value: 'IMPORT' },
  { label: '新建', value: 'CREATE' },
  { label: '删除', value: 'DELETE' },
  { label: '编辑', value: 'UPDATE' },
  { label: '修改状态', value: 'STATUS' },
  { label: '分配角色菜单权限', value: 'ROLE_MENU_ASSIGN' },
  { label: '新增院系', value: 'DEPARTMENT_CREATE' },
  { label: '编辑院系', value: 'DEPARTMENT_UPDATE' },
  { label: '修改院系状态', value: 'DEPARTMENT_STATUS' },
  { label: '删除院系', value: 'DEPARTMENT_DELETE' },
  { label: '新增专业', value: 'MAJOR_CREATE' },
  { label: '编辑专业', value: 'MAJOR_UPDATE' },
  { label: '修改专业状态', value: 'MAJOR_STATUS' },
  { label: '删除专业', value: 'MAJOR_DELETE' },
  { label: '新增年级', value: 'GRADE_CREATE' },
  { label: '编辑年级', value: 'GRADE_UPDATE' },
  { label: '修改年级状态', value: 'GRADE_STATUS' },
  { label: '删除年级', value: 'GRADE_DELETE' },
  { label: '新增实习基地', value: 'INTERNSHIP_BASE_CREATE' },
  { label: '编辑实习基地', value: 'INTERNSHIP_BASE_UPDATE' },
  { label: '修改实习基地状态', value: 'INTERNSHIP_BASE_STATUS' },
  { label: '删除实习基地', value: 'INTERNSHIP_BASE_DELETE' },
  { label: '调整成绩', value: 'ADJUST' },
  { label: '未知动作', value: 'UNKNOWN' }
];

const operationQuery = reactive({
  keyword: '',
  moduleCode: '',
  actionCode: '',
  operationStatus: ''
});
const operationRange = ref<string[]>([]);
const operationPage = reactive<PageState<SysOperationLogItem>>({
  loading: false,
  page: 1,
  size: 10,
  total: 0,
  records: []
});

const loginQuery = reactive({
  keyword: '',
  loginResult: ''
});
const loginRange = ref<string[]>([]);
const loginPage = reactive<PageState<SysLoginLogItem>>({
  loading: false,
  page: 1,
  size: 10,
  total: 0,
  records: []
});

function operationStatusLabel(status?: string) {
  return status === 'SUCCESS' ? '成功' : status === 'FAIL' ? '失败' : '-';
}

function loginResultLabel(result?: string) {
  return result === 'SUCCESS' ? '成功' : result === 'FAIL' ? '失败' : '-';
}

function operatorNameLabel(operatorName?: string) {
  return operatorName === 'SYSTEM' ? '系统' : operatorName || '-';
}

function moduleLabel(moduleCode?: string) {
  return moduleOptions.find((item) => item.value === moduleCode)?.label || moduleCode || '-';
}

function actionLabel(actionCode?: string) {
  return actionOptions.find((item) => item.value === actionCode)?.label || actionCode || '-';
}
async function fetchOperationPage() {
  operationPage.loading = true;
  try {
    const resp = await queryOperationLogPageApi({
      page: operationPage.page,
      size: operationPage.size,
      keyword: operationQuery.keyword || undefined,
      moduleCode: operationQuery.moduleCode || undefined,
      actionCode: operationQuery.actionCode || undefined,
      operationStatus: operationQuery.operationStatus || undefined,
      startTime: operationRange.value.length === 2 ? operationRange.value[0] : undefined,
      endTime: operationRange.value.length === 2 ? operationRange.value[1] : undefined
    });
    operationPage.records = resp.data.records || [];
    operationPage.total = Number(resp.data.total || 0);
  } finally {
    operationPage.loading = false;
  }
}

function searchOperation() {
  operationPage.page = 1;
  fetchOperationPage();
}

function resetOperation() {
  operationQuery.keyword = '';
  operationQuery.moduleCode = '';
  operationQuery.actionCode = '';
  operationQuery.operationStatus = '';
  operationRange.value = [];
  operationPage.page = 1;
  fetchOperationPage();
}

function onOperationSizeChange(size: number) {
  operationPage.size = size;
  operationPage.page = 1;
  fetchOperationPage();
}

function onOperationCurrentChange(page: number) {
  operationPage.page = page;
  fetchOperationPage();
}

async function fetchLoginPage() {
  loginPage.loading = true;
  try {
    const resp = await queryLoginLogPageApi({
      page: loginPage.page,
      size: loginPage.size,
      keyword: loginQuery.keyword || undefined,
      loginResult: loginQuery.loginResult || undefined,
      startTime: loginRange.value.length === 2 ? loginRange.value[0] : undefined,
      endTime: loginRange.value.length === 2 ? loginRange.value[1] : undefined
    });
    loginPage.records = resp.data.records || [];
    loginPage.total = Number(resp.data.total || 0);
  } finally {
    loginPage.loading = false;
  }
}

function searchLogin() {
  loginPage.page = 1;
  fetchLoginPage();
}

function resetLogin() {
  loginQuery.keyword = '';
  loginQuery.loginResult = '';
  loginRange.value = [];
  loginPage.page = 1;
  fetchLoginPage();
}

function onLoginSizeChange(size: number) {
  loginPage.size = size;
  loginPage.page = 1;
  fetchLoginPage();
}

function onLoginCurrentChange(page: number) {
  loginPage.page = page;
  fetchLoginPage();
}

watch(activeTab, (tab) => {
  if (tab === 'operation') {
    fetchOperationPage();
  } else {
    fetchLoginPage();
  }
});

onMounted(() => {
  fetchOperationPage();
});
</script>

<style scoped lang="scss">
.query-form {
  margin-bottom: 12px;
}

.pager {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>


