<template>
  <el-form :inline="true" class="query-form">
    <el-form-item label="关键词">
      <el-input
        v-model.trim="query.keyword"
        clearable
        placeholder="编码/名称/参数值"
        @keyup.enter="search"
      />
    </el-form-item>
    <el-form-item label="类型">
      <el-select v-model="query.paramType" clearable placeholder="全部">
        <el-option label="系统参数" value="SYSTEM" />
        <el-option label="业务参数" value="BUSINESS" />
      </el-select>
    </el-form-item>
    <el-form-item label="状态">
      <el-select v-model="query.status" clearable placeholder="全部">
        <el-option label="启用" value="ENABLED" />
        <el-option label="禁用" value="DISABLED" />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="reset">重置</el-button>
      <el-button type="success" @click="openCreate">新建</el-button>
    </el-form-item>
  </el-form>

  <el-table v-loading="page.loading" :data="page.records" border>
    <el-table-column prop="paramCode" label="参数编码" min-width="170" />
    <el-table-column prop="paramName" label="参数名称" min-width="170" />
    <el-table-column prop="paramValue" label="参数值" min-width="160" />
    <el-table-column prop="paramType" label="类型" width="110">
      <template #default="{ row }">{{ paramTypeLabel(row.paramType) }}</template>
    </el-table-column>
    <el-table-column prop="status" label="状态" width="100">
      <template #default="{ row }">
        <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ statusLabel(row.status) }}</el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="remark" label="备注" min-width="220" show-overflow-tooltip />
    <el-table-column prop="updatedTime" label="更新时间" min-width="170" />
    <el-table-column label="操作" width="180" fixed="right">
      <template #default="{ row }">
        <el-button link type="primary" @click="openEdit(row.id)">编辑</el-button>
        <el-button
          link
          :type="row.status === 'ENABLED' ? 'warning' : 'success'"
          @click="updateStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')"
        >
          {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
        </el-button>
      </template>
    </el-table-column>
  </el-table>

  <div class="pager">
    <el-pagination
      background
      layout="total, sizes, prev, pager, next, jumper"
      :total="page.total"
      :current-page="page.page"
      :page-size="page.size"
      :page-sizes="[10, 20, 50, 100]"
      @size-change="onSizeChange"
      @current-change="onCurrentChange"
    />
  </div>

  <el-dialog
    v-model="dialog.visible"
    :title="dialog.mode === 'create' ? '新建参数' : '编辑参数'"
    width="560px"
    destroy-on-close
  >
    <el-form label-width="110px">
      <el-form-item label="参数编码"><el-input v-model.trim="form.paramCode" /></el-form-item>
      <el-form-item label="参数名称"><el-input v-model.trim="form.paramName" /></el-form-item>
      <el-form-item label="参数值"><el-input v-model.trim="form.paramValue" /></el-form-item>
      <el-form-item label="参数类型">
        <el-select v-model="form.paramType">
          <el-option label="系统参数" value="SYSTEM" />
          <el-option label="业务参数" value="BUSINESS" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注">
        <el-input v-model.trim="form.remark" type="textarea" :rows="3" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialog.visible = false">取消</el-button>
      <el-button type="primary" :loading="dialog.loading" @click="submit">确认</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  createParamApi,
  getParamDetailApi,
  queryParamPageApi,
  updateParamApi,
  updateParamStatusApi
} from '@/api/modules/system-admin';
import type { SysParamItem } from '@/types/api';

type StatusType = '' | 'ENABLED' | 'DISABLED';
type DialogMode = 'create' | 'edit';

interface PageState<T> {
  loading: boolean;
  page: number;
  size: number;
  total: number;
  records: T[];
}

const query = reactive({ keyword: '', paramType: '', status: '' as StatusType });
const page = reactive<PageState<SysParamItem>>({ loading: false, page: 1, size: 10, total: 0, records: [] });
const dialog = reactive({ visible: false, mode: 'create' as DialogMode, loading: false });
const form = reactive<{ id?: number; paramCode: string; paramName: string; paramValue: string; paramType: string; remark: string }>({
  paramCode: '',
  paramName: '',
  paramValue: '',
  paramType: 'SYSTEM',
  remark: ''
});

function statusLabel(status?: string) {
  return status === 'ENABLED' ? '启用' : status === 'DISABLED' ? '禁用' : '-';
}

function paramTypeLabel(type?: string) {
  return type === 'SYSTEM' ? '系统参数' : type === 'BUSINESS' ? '业务参数' : '-';
}
async function fetchPage() {
  page.loading = true;
  try {
    const resp = await queryParamPageApi({
      page: page.page,
      size: page.size,
      keyword: query.keyword || undefined,
      paramType: query.paramType || undefined,
      status: query.status || undefined
    });
    page.records = resp.data.records || [];
    page.total = Number(resp.data.total || 0);
  } finally {
    page.loading = false;
  }
}

function search() {
  page.page = 1;
  fetchPage();
}

function reset() {
  query.keyword = '';
  query.paramType = '';
  query.status = '';
  page.page = 1;
  fetchPage();
}

function onSizeChange(size: number) {
  page.size = size;
  page.page = 1;
  fetchPage();
}

function onCurrentChange(current: number) {
  page.page = current;
  fetchPage();
}

function openCreate() {
  dialog.mode = 'create';
  Object.assign(form, {
    id: undefined,
    paramCode: '',
    paramName: '',
    paramValue: '',
    paramType: 'SYSTEM',
    remark: ''
  });
  dialog.visible = true;
}

async function openEdit(id: number) {
  const resp = await getParamDetailApi(id);
  Object.assign(form, {
    id: resp.data.id,
    paramCode: resp.data.paramCode,
    paramName: resp.data.paramName,
    paramValue: resp.data.paramValue,
    paramType: resp.data.paramType,
    remark: resp.data.remark || ''
  });
  dialog.mode = 'edit';
  dialog.visible = true;
}

async function submit() {
  if (!form.paramCode || !form.paramName || !form.paramValue || !form.paramType) {
    ElMessage.warning('参数编码/名称/值/类型不能为空');
    return;
  }
  dialog.loading = true;
  try {
    const payload = {
      paramCode: form.paramCode,
      paramName: form.paramName,
      paramValue: form.paramValue,
      paramType: form.paramType,
      remark: form.remark || undefined
    };
    if (dialog.mode === 'create') {
      await createParamApi(payload);
    } else if (form.id) {
      await updateParamApi(form.id, payload);
    }
    ElMessage.success('保存成功');
    dialog.visible = false;
    fetchPage();
  } finally {
    dialog.loading = false;
  }
}

async function updateStatus(id: number, status: 'ENABLED' | 'DISABLED') {
  await ElMessageBox.confirm(`确认将状态设置为“${statusLabel(status)}”吗？`, '提示', {
    type: 'warning'
  });
  await updateParamStatusApi(id, { status });
  ElMessage.success('状态更新成功');
  fetchPage();
}

onMounted(() => {
  fetchPage();
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


