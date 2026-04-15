<template>
  <el-tabs v-model="activeTab">
    <el-tab-pane label="院系" name="department">
      <el-form :inline="true" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model.trim="deptQuery.keyword" clearable placeholder="编码/名称/负责人" @keyup.enter="searchDept" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="deptQuery.status" clearable placeholder="全部">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchDept">查询</el-button>
          <el-button @click="resetDept">重置</el-button>
          <el-button type="success" @click="openDeptCreate">新建</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="deptState.loading" :data="deptState.records" border>
        <el-table-column prop="deptCode" label="编码" min-width="120" />
        <el-table-column prop="deptName" label="名称" min-width="140" />
        <el-table-column prop="leaderName" label="负责人" min-width="120" />
        <el-table-column prop="contactPhone" label="手机号" min-width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDeptEdit(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 'ENABLED' ? 'warning' : 'success'"
              @click="updateDeptStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')"
            >
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" :disabled="isDeleting('department', row.id)" @click="handleDeleteDept(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="deptState.total" :current-page="deptState.page" :page-size="deptState.size" :page-sizes="[10, 20, 50, 100]" @size-change="onDeptSizeChange" @current-change="onDeptCurrentChange" />
      </div>
    </el-tab-pane>

    <el-tab-pane label="专业" name="major">
      <el-form :inline="true" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model.trim="majorQuery.keyword" clearable placeholder="编码/名称" @keyup.enter="searchMajor" />
        </el-form-item>
        <el-form-item label="院系">
          <el-select v-model="majorQuery.deptId" clearable filterable placeholder="全部">
            <el-option v-for="item in deptOptions" :key="item.id" :label="item.name" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="majorQuery.status" clearable placeholder="全部">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchMajor">查询</el-button>
          <el-button @click="resetMajor">重置</el-button>
          <el-button type="success" @click="openMajorCreate">新建</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="majorState.loading" :data="majorState.records" border>
        <el-table-column prop="deptName" label="院系" min-width="140" />
        <el-table-column prop="majorCode" label="编码" min-width="120" />
        <el-table-column prop="majorName" label="名称" min-width="160" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openMajorEdit(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 'ENABLED' ? 'warning' : 'success'"
              @click="updateMajorStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')"
            >
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" :disabled="isDeleting('major', row.id)" @click="handleDeleteMajor(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="majorState.total" :current-page="majorState.page" :page-size="majorState.size" :page-sizes="[10, 20, 50, 100]" @size-change="onMajorSizeChange" @current-change="onMajorCurrentChange" />
      </div>
    </el-tab-pane>

    <el-tab-pane label="年级" name="grade">
      <el-form :inline="true" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model.trim="gradeQuery.keyword" clearable placeholder="编码/名称" @keyup.enter="searchGrade" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="gradeQuery.status" clearable placeholder="全部">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchGrade">查询</el-button>
          <el-button @click="resetGrade">重置</el-button>
          <el-button type="success" @click="openGradeCreate">新建</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="gradeState.loading" :data="gradeState.records" border>
        <el-table-column prop="gradeCode" label="编码" min-width="120" />
        <el-table-column prop="gradeName" label="名称" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openGradeEdit(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 'ENABLED' ? 'warning' : 'success'"
              @click="updateGradeStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')"
            >
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" :disabled="isDeleting('grade', row.id)" @click="handleDeleteGrade(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="gradeState.total" :current-page="gradeState.page" :page-size="gradeState.size" :page-sizes="[10, 20, 50, 100]" @size-change="onGradeSizeChange" @current-change="onGradeCurrentChange" />
      </div>
    </el-tab-pane>

    <el-tab-pane label="实习基地" name="base">
      <el-form :inline="true" class="query-form">
        <el-form-item label="关键词">
          <el-input v-model.trim="baseQuery.keyword" clearable placeholder="编码/名称/城市" @keyup.enter="searchBase" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="baseQuery.status" clearable placeholder="全部">
            <el-option label="启用" value="ENABLED" />
            <el-option label="禁用" value="DISABLED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchBase">查询</el-button>
          <el-button @click="resetBase">重置</el-button>
          <el-button type="success" @click="openBaseCreate">新建</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="baseState.loading" :data="baseState.records" border>
        <el-table-column prop="baseCode" label="编码" min-width="120" />
        <el-table-column prop="baseName" label="名称" min-width="160" />
        <el-table-column label="地区" min-width="190">
          <template #default="{ row }">
            {{ [row.province, row.city, row.district].filter(Boolean).join('/') || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="contactPerson" label="联系人" min-width="120" />
        <el-table-column prop="contactPhone" label="手机号" min-width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openBaseEdit(row)">编辑</el-button>
            <el-button
              link
              :type="row.status === 'ENABLED' ? 'warning' : 'success'"
              @click="updateBaseStatus(row.id, row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED')"
            >
              {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
            </el-button>
            <el-button link type="danger" :disabled="isDeleting('base', row.id)" @click="handleDeleteBase(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="pager">
        <el-pagination background layout="total, sizes, prev, pager, next, jumper" :total="baseState.total" :current-page="baseState.page" :page-size="baseState.size" :page-sizes="[10, 20, 50, 100]" @size-change="onBaseSizeChange" @current-change="onBaseCurrentChange" />
      </div>
    </el-tab-pane>
  </el-tabs>

  <el-dialog v-model="deptDialog.visible" :title="deptDialog.mode === 'create' ? '新建院系' : '编辑院系'" width="520px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="编码"><el-input v-model.trim="deptForm.deptCode" placeholder="留空则自动生成" /></el-form-item>
      <el-form-item label="名称"><el-input v-model.trim="deptForm.deptName" /></el-form-item>
      <el-form-item label="上级院系"><el-select v-model="deptForm.parentId" clearable placeholder="顶级"><el-option v-for="item in deptOptions" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
      <el-form-item label="负责人"><el-input v-model.trim="deptForm.leaderName" /></el-form-item>
      <el-form-item label="手机号"><el-input v-model.trim="deptForm.contactPhone" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="deptDialog.visible = false">取消</el-button><el-button type="primary" :loading="deptDialog.loading" @click="submitDept">确认</el-button></template>
  </el-dialog>

  <el-dialog v-model="majorDialog.visible" :title="majorDialog.mode === 'create' ? '新建专业' : '编辑专业'" width="520px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="院系"><el-select v-model="majorForm.deptId" filterable><el-option v-for="item in deptOptions" :key="item.id" :label="item.name" :value="item.id" /></el-select></el-form-item>
      <el-form-item label="编码"><el-input v-model.trim="majorForm.majorCode" placeholder="留空则自动生成" /></el-form-item>
      <el-form-item label="名称"><el-input v-model.trim="majorForm.majorName" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="majorDialog.visible = false">取消</el-button><el-button type="primary" :loading="majorDialog.loading" @click="submitMajor">确认</el-button></template>
  </el-dialog>

  <el-dialog v-model="gradeDialog.visible" :title="gradeDialog.mode === 'create' ? '新建年级' : '编辑年级'" width="520px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="编码"><el-input v-model.trim="gradeForm.gradeCode" placeholder="留空则自动生成" /></el-form-item>
      <el-form-item label="名称"><el-input v-model.trim="gradeForm.gradeName" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="gradeDialog.visible = false">取消</el-button><el-button type="primary" :loading="gradeDialog.loading" @click="submitGrade">确认</el-button></template>
  </el-dialog>

  <el-dialog v-model="baseDialog.visible" :title="baseDialog.mode === 'create' ? '新建实习基地' : '编辑实习基地'" width="560px" destroy-on-close>
    <el-form label-width="100px">
      <el-form-item label="编码"><el-input v-model.trim="baseForm.baseCode" placeholder="留空则自动生成" /></el-form-item>
      <el-form-item label="名称"><el-input v-model.trim="baseForm.baseName" /></el-form-item>
      <el-form-item label="省份"><el-input v-model.trim="baseForm.province" /></el-form-item>
      <el-form-item label="城市"><el-input v-model.trim="baseForm.city" /></el-form-item>
      <el-form-item label="区县"><el-input v-model.trim="baseForm.district" /></el-form-item>
      <el-form-item label="地址"><el-input v-model.trim="baseForm.address" /></el-form-item>
      <el-form-item label="联系人"><el-input v-model.trim="baseForm.contactPerson" /></el-form-item>
      <el-form-item label="手机号"><el-input v-model.trim="baseForm.contactPhone" /></el-form-item>
    </el-form>
    <template #footer><el-button @click="baseDialog.visible = false">取消</el-button><el-button type="primary" :loading="baseDialog.loading" @click="submitBase">确认</el-button></template>
  </el-dialog>
</template>

<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import {
  createDepartmentApi,
  createGradeApi,
  createInternshipBaseApi,
  createMajorApi,
  deleteDepartmentApi,
  deleteGradeApi,
  deleteInternshipBaseApi,
  deleteMajorApi,
  queryDepartmentOptionsApi,
  queryDepartmentPageApi,
  queryGradePageApi,
  queryInternshipBasePageApi,
  queryMajorPageApi,
  updateDepartmentApi,
  updateDepartmentStatusApi,
  updateGradeApi,
  updateGradeStatusApi,
  updateInternshipBaseApi,
  updateInternshipBaseStatusApi,
  updateMajorApi,
  updateMajorStatusApi
} from '@/api/modules/system-admin';
import type { BaseDepartmentItem, BaseGradeItem, BaseInternshipBaseItem, BaseMajorItem, IdNameOption } from '@/types/api';

type TabType = 'department' | 'major' | 'grade' | 'base';
type StatusType = '' | 'ENABLED' | 'DISABLED';
type DialogMode = 'create' | 'edit';

interface PageState<T> {
  loading: boolean;
  page: number;
  size: number;
  total: number;
  records: T[];
}

const activeTab = ref<TabType>('department');
const deptOptions = ref<IdNameOption[]>([]);
const deletingMap = reactive<Record<TabType, number[]>>({
  department: [],
  major: [],
  grade: [],
  base: []
});

const deptQuery = reactive({ keyword: '', status: '' as StatusType });
const deptState = reactive<PageState<BaseDepartmentItem>>({ loading: false, page: 1, size: 10, total: 0, records: [] });
const deptDialog = reactive({ visible: false, mode: 'create' as DialogMode, loading: false });
const deptForm = reactive<{ id?: number; deptCode: string; deptName: string; parentId?: number; leaderName: string; contactPhone: string }>({ deptCode: '', deptName: '', parentId: undefined, leaderName: '', contactPhone: '' });

const majorQuery = reactive({ keyword: '', status: '' as StatusType, deptId: undefined as number | undefined });
const majorState = reactive<PageState<BaseMajorItem>>({ loading: false, page: 1, size: 10, total: 0, records: [] });
const majorDialog = reactive({ visible: false, mode: 'create' as DialogMode, loading: false });
const majorForm = reactive<{ id?: number; deptId?: number; majorCode: string; majorName: string }>({ deptId: undefined, majorCode: '', majorName: '' });

const gradeQuery = reactive({ keyword: '', status: '' as StatusType });
const gradeState = reactive<PageState<BaseGradeItem>>({ loading: false, page: 1, size: 10, total: 0, records: [] });
const gradeDialog = reactive({ visible: false, mode: 'create' as DialogMode, loading: false });
const gradeForm = reactive<{ id?: number; gradeCode: string; gradeName: string }>({ gradeCode: '', gradeName: '' });

const baseQuery = reactive({ keyword: '', status: '' as StatusType });
const baseState = reactive<PageState<BaseInternshipBaseItem>>({ loading: false, page: 1, size: 10, total: 0, records: [] });
const baseDialog = reactive({ visible: false, mode: 'create' as DialogMode, loading: false });
const baseForm = reactive<{ id?: number; baseCode: string; baseName: string; province: string; city: string; district: string; address: string; contactPerson: string; contactPhone: string }>({
  baseCode: '',
  baseName: '',
  province: '',
  city: '',
  district: '',
  address: '',
  contactPerson: '',
  contactPhone: ''
});

function statusLabel(status?: string) {
  return status === 'ENABLED' ? '启用' : status === 'DISABLED' ? '禁用' : '-';
}

function isDeleting(tab: TabType, id?: number) {
  return typeof id === 'number' && deletingMap[tab].includes(id);
}

function startDeleting(tab: TabType, id: number) {
  if (!deletingMap[tab].includes(id)) {
    deletingMap[tab].push(id);
  }
}

function finishDeleting(tab: TabType, id: number) {
  deletingMap[tab] = deletingMap[tab].filter((item) => item !== id);
}

async function fetchDeptOptions() {
  const resp = await queryDepartmentOptionsApi();
  deptOptions.value = resp.data || [];
}

async function fetchDeptPage() {
  deptState.loading = true;
  try {
    const resp = await queryDepartmentPageApi({ page: deptState.page, size: deptState.size, keyword: deptQuery.keyword || undefined, status: deptQuery.status || undefined });
    deptState.records = resp.data.records || [];
    deptState.total = Number(resp.data.total || 0);
  } finally {
    deptState.loading = false;
  }
}
function searchDept() { deptState.page = 1; fetchDeptPage(); }
function resetDept() { deptQuery.keyword = ''; deptQuery.status = ''; deptState.page = 1; fetchDeptPage(); }
function onDeptSizeChange(size: number) { deptState.size = size; deptState.page = 1; fetchDeptPage(); }
function onDeptCurrentChange(page: number) { deptState.page = page; fetchDeptPage(); }
function openDeptCreate() { deptDialog.mode = 'create'; Object.assign(deptForm, { id: undefined, deptCode: '', deptName: '', parentId: undefined, leaderName: '', contactPhone: '' }); deptDialog.visible = true; }
function openDeptEdit(row: BaseDepartmentItem) { deptDialog.mode = 'edit'; Object.assign(deptForm, { id: row.id, deptCode: row.deptCode, deptName: row.deptName, parentId: row.parentId || undefined, leaderName: row.leaderName || '', contactPhone: row.contactPhone || '' }); deptDialog.visible = true; }
async function submitDept() {
  if (!deptForm.deptName) { ElMessage.warning('名称不能为空'); return; }
  deptDialog.loading = true;
  try {
    const payload = { deptCode: deptForm.deptCode || undefined, deptName: deptForm.deptName, parentId: deptForm.parentId, leaderName: deptForm.leaderName || undefined, contactPhone: deptForm.contactPhone || undefined };
    if (deptDialog.mode === 'create') await createDepartmentApi(payload); else if (deptForm.id) await updateDepartmentApi(deptForm.id, payload);
    ElMessage.success('保存成功');
    deptDialog.visible = false;
    await Promise.all([fetchDeptPage(), fetchDeptOptions()]);
  } finally { deptDialog.loading = false; }
}
async function updateDeptStatus(id: number, status: 'ENABLED' | 'DISABLED') {
  await ElMessageBox.confirm(`确认将状态设置为“${statusLabel(status)}”吗？`, '提示', { type: 'warning' });
  await updateDepartmentStatusApi(id, { status });
  ElMessage.success('状态更新成功');
  fetchDeptPage();
}
async function handleDeleteDept(id: number) {
  if (isDeleting('department', id)) { return; }
  await ElMessageBox.confirm('确认删除该院系吗？如已被下级院系、专业、用户或计划引用将无法删除。', '删除确认', { type: 'warning' });
  startDeleting('department', id);
  try {
    await deleteDepartmentApi(id);
    ElMessage.success('院系删除成功');
    await Promise.all([fetchDeptPage(), fetchDeptOptions()]);
  } finally {
    finishDeleting('department', id);
  }
}

async function fetchMajorPage() {
  majorState.loading = true;
  try {
    const resp = await queryMajorPageApi({ page: majorState.page, size: majorState.size, keyword: majorQuery.keyword || undefined, status: majorQuery.status || undefined, deptId: majorQuery.deptId });
    majorState.records = resp.data.records || [];
    majorState.total = Number(resp.data.total || 0);
  } finally { majorState.loading = false; }
}
function searchMajor() { majorState.page = 1; fetchMajorPage(); }
function resetMajor() { majorQuery.keyword = ''; majorQuery.status = ''; majorQuery.deptId = undefined; majorState.page = 1; fetchMajorPage(); }
function onMajorSizeChange(size: number) { majorState.size = size; majorState.page = 1; fetchMajorPage(); }
function onMajorCurrentChange(page: number) { majorState.page = page; fetchMajorPage(); }
function openMajorCreate() { majorDialog.mode = 'create'; Object.assign(majorForm, { id: undefined, deptId: undefined, majorCode: '', majorName: '' }); majorDialog.visible = true; }
function openMajorEdit(row: BaseMajorItem) { majorDialog.mode = 'edit'; Object.assign(majorForm, { id: row.id, deptId: row.deptId, majorCode: row.majorCode, majorName: row.majorName }); majorDialog.visible = true; }
async function submitMajor() {
  if (!majorForm.deptId || !majorForm.majorName) { ElMessage.warning('院系和名称不能为空'); return; }
  majorDialog.loading = true;
  try {
    const payload = { deptId: majorForm.deptId, majorCode: majorForm.majorCode || undefined, majorName: majorForm.majorName };
    if (majorDialog.mode === 'create') await createMajorApi(payload); else if (majorForm.id) await updateMajorApi(majorForm.id, payload);
    ElMessage.success('保存成功');
    majorDialog.visible = false;
    fetchMajorPage();
  } finally { majorDialog.loading = false; }
}
async function updateMajorStatus(id: number, status: 'ENABLED' | 'DISABLED') {
  await ElMessageBox.confirm(`确认将状态设置为“${statusLabel(status)}”吗？`, '提示', { type: 'warning' });
  await updateMajorStatusApi(id, { status });
  ElMessage.success('状态更新成功');
  fetchMajorPage();
}
async function handleDeleteMajor(id: number) {
  if (isDeleting('major', id)) { return; }
  await ElMessageBox.confirm('确认删除该专业吗？如已被用户引用将无法删除。', '删除确认', { type: 'warning' });
  startDeleting('major', id);
  try {
    await deleteMajorApi(id);
    ElMessage.success('专业删除成功');
    fetchMajorPage();
  } finally {
    finishDeleting('major', id);
  }
}

async function fetchGradePage() {
  gradeState.loading = true;
  try {
    const resp = await queryGradePageApi({ page: gradeState.page, size: gradeState.size, keyword: gradeQuery.keyword || undefined, status: gradeQuery.status || undefined });
    gradeState.records = resp.data.records || [];
    gradeState.total = Number(resp.data.total || 0);
  } finally { gradeState.loading = false; }
}
function searchGrade() { gradeState.page = 1; fetchGradePage(); }
function resetGrade() { gradeQuery.keyword = ''; gradeQuery.status = ''; gradeState.page = 1; fetchGradePage(); }
function onGradeSizeChange(size: number) { gradeState.size = size; gradeState.page = 1; fetchGradePage(); }
function onGradeCurrentChange(page: number) { gradeState.page = page; fetchGradePage(); }
function openGradeCreate() { gradeDialog.mode = 'create'; Object.assign(gradeForm, { id: undefined, gradeCode: '', gradeName: '' }); gradeDialog.visible = true; }
function openGradeEdit(row: BaseGradeItem) { gradeDialog.mode = 'edit'; Object.assign(gradeForm, { id: row.id, gradeCode: row.gradeCode, gradeName: row.gradeName }); gradeDialog.visible = true; }
async function submitGrade() {
  if (!gradeForm.gradeName) { ElMessage.warning('名称不能为空'); return; }
  gradeDialog.loading = true;
  try {
    const payload = { gradeCode: gradeForm.gradeCode || undefined, gradeName: gradeForm.gradeName };
    if (gradeDialog.mode === 'create') await createGradeApi(payload); else if (gradeForm.id) await updateGradeApi(gradeForm.id, payload);
    ElMessage.success('保存成功');
    gradeDialog.visible = false;
    fetchGradePage();
  } finally { gradeDialog.loading = false; }
}
async function updateGradeStatus(id: number, status: 'ENABLED' | 'DISABLED') {
  await ElMessageBox.confirm(`确认将状态设置为“${statusLabel(status)}”吗？`, '提示', { type: 'warning' });
  await updateGradeStatusApi(id, { status });
  ElMessage.success('状态更新成功');
  fetchGradePage();
}
async function handleDeleteGrade(id: number) {
  if (isDeleting('grade', id)) { return; }
  await ElMessageBox.confirm('确认删除该年级吗？如已被用户引用将无法删除。', '删除确认', { type: 'warning' });
  startDeleting('grade', id);
  try {
    await deleteGradeApi(id);
    ElMessage.success('年级删除成功');
    fetchGradePage();
  } finally {
    finishDeleting('grade', id);
  }
}

async function fetchBasePage() {
  baseState.loading = true;
  try {
    const resp = await queryInternshipBasePageApi({ page: baseState.page, size: baseState.size, keyword: baseQuery.keyword || undefined, status: baseQuery.status || undefined });
    baseState.records = resp.data.records || [];
    baseState.total = Number(resp.data.total || 0);
  } finally { baseState.loading = false; }
}
function searchBase() { baseState.page = 1; fetchBasePage(); }
function resetBase() { baseQuery.keyword = ''; baseQuery.status = ''; baseState.page = 1; fetchBasePage(); }
function onBaseSizeChange(size: number) { baseState.size = size; baseState.page = 1; fetchBasePage(); }
function onBaseCurrentChange(page: number) { baseState.page = page; fetchBasePage(); }
function openBaseCreate() { baseDialog.mode = 'create'; Object.assign(baseForm, { id: undefined, baseCode: '', baseName: '', province: '', city: '', district: '', address: '', contactPerson: '', contactPhone: '' }); baseDialog.visible = true; }
function openBaseEdit(row: BaseInternshipBaseItem) { baseDialog.mode = 'edit'; Object.assign(baseForm, { id: row.id, baseCode: row.baseCode, baseName: row.baseName, province: row.province || '', city: row.city || '', district: row.district || '', address: row.address || '', contactPerson: row.contactPerson || '', contactPhone: row.contactPhone || '' }); baseDialog.visible = true; }
async function submitBase() {
  if (!baseForm.baseName) { ElMessage.warning('名称不能为空'); return; }
  baseDialog.loading = true;
  try {
    const payload = { baseCode: baseForm.baseCode || undefined, baseName: baseForm.baseName, province: baseForm.province || undefined, city: baseForm.city || undefined, district: baseForm.district || undefined, address: baseForm.address || undefined, contactPerson: baseForm.contactPerson || undefined, contactPhone: baseForm.contactPhone || undefined };
    if (baseDialog.mode === 'create') await createInternshipBaseApi(payload); else if (baseForm.id) await updateInternshipBaseApi(baseForm.id, payload);
    ElMessage.success('保存成功');
    baseDialog.visible = false;
    fetchBasePage();
  } finally { baseDialog.loading = false; }
}
async function updateBaseStatus(id: number, status: 'ENABLED' | 'DISABLED') {
  await ElMessageBox.confirm(`确认将状态设置为“${statusLabel(status)}”吗？`, '提示', { type: 'warning' });
  await updateInternshipBaseStatusApi(id, { status });
  ElMessage.success('状态更新成功');
  fetchBasePage();
}
async function handleDeleteBase(id: number) {
  if (isDeleting('base', id)) { return; }
  await ElMessageBox.confirm('确认删除该实习基地吗？如已被分配记录引用将无法删除。', '删除确认', { type: 'warning' });
  startDeleting('base', id);
  try {
    await deleteInternshipBaseApi(id);
    ElMessage.success('实习基地删除成功');
    fetchBasePage();
  } finally {
    finishDeleting('base', id);
  }
}

function fetchByActiveTab() {
  if (activeTab.value === 'department') fetchDeptPage();
  if (activeTab.value === 'major') fetchMajorPage();
  if (activeTab.value === 'grade') fetchGradePage();
  if (activeTab.value === 'base') fetchBasePage();
}

watch(activeTab, () => fetchByActiveTab());
onMounted(async () => {
  await fetchDeptOptions();
  fetchByActiveTab();
});
</script>

<style scoped lang="scss">
.query-form { margin-bottom: 12px; }
.pager { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
