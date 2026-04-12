<template>
  <el-card class="notice-page">
      <template #header>
        <div class="header-row">
          <div class="title-wrap">
            <div class="page-title">消息中心</div>
            <el-tag v-if="noticeStore.unreadCount > 0" type="danger">未读 {{ noticeStore.unreadCount }}</el-tag>
          </div>
          <div class="header-actions">
            <el-button
              :disabled="selectedNoticeIds.length <= 0"
              type="danger"
              plain
              @click="handleBatchDelete"
            >
              批量删除
            </el-button>
            <el-button :disabled="noticeStore.unreadCount <= 0" @click="handleMarkAllRead">全部标记已读</el-button>
          </div>
        </div>
      </template>

    <el-alert
      v-if="!canViewNotice"
      title="当前角色没有消息中心访问权限"
      type="warning"
      :closable="false"
      show-icon
    />

    <template v-else>
      <el-form :inline="true" :model="queryForm" class="query-form">
        <el-form-item label="关键词" class="query-form__item query-form__item--keyword">
          <el-input
            v-model.trim="queryForm.keyword"
            clearable
            placeholder="标题/内容"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="状态" class="query-form__item query-form__item--status">
          <el-select v-model="queryForm.readFlag" clearable placeholder="全部">
            <el-option label="未读" value="0" />
            <el-option label="已读" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item class="query-form__item query-form__item--actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="tableData" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="noticeTitle" label="标题" min-width="220" />
        <el-table-column label="类型" width="110">
          <template #default="{ row }">
            <el-tag>{{ noticeTypeLabel(row.noticeType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="级别" width="100">
          <template #default="{ row }">
            <el-tag :type="row.noticeLevel === 'URGENT' ? 'danger' : 'info'">
              {{ row.noticeLevel === 'URGENT' ? '紧急' : '普通' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column
          prop="noticeContentPreview"
          label="内容摘要"
          min-width="260"
          show-overflow-tooltip
        />
        <el-table-column prop="senderName" label="发送人" width="120" />
        <el-table-column prop="sendTime" label="发送时间" width="180" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.readFlag === 1 ? 'success' : 'warning'">
              {{ row.readFlag === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="goDetail(row.noticeId)">详情</el-button>
            <el-button
              v-if="row.readFlag === 0"
              link
              type="success"
              @click="handleMarkRead(row.noticeId)"
            >
              标记已读
            </el-button>
            <el-button link type="danger" @click="handleDelete(row.noticeId)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pager-wrap">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :current-page="page"
          :page-size="size"
          :page-sizes="[10, 20, 50]"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </template>
  </el-card>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { queryNoticePageApi } from '@/api/modules/notice';
import { useAuthStore } from '@/store/modules/auth';
import { useNoticeStore } from '@/store/modules/notice';
import type { IdValue, NoticeListItem } from '@/types/api';

const router = useRouter();
const authStore = useAuthStore();
const noticeStore = useNoticeStore();

const canViewNotice = computed(() => authStore.permissionCodes.includes('notice:view'));
const loading = ref(false);
const tableData = ref<NoticeListItem[]>([]);
const total = ref(0);
const page = ref(1);
const size = ref(10);
const selectedNoticeIds = ref<IdValue[]>([]);

const queryForm = reactive({
  keyword: '',
  readFlag: '' as '' | '0' | '1' | undefined
});

function noticeTypeLabel(type?: string): string {
  if (type === 'SYSTEM') return '系统通知';
  if (type === 'BUSINESS') return '业务通知';
  return type || '-';
}

async function fetchData() {
  if (!canViewNotice.value) {
    tableData.value = [];
    total.value = 0;
    return;
  }
  loading.value = true;
  try {
    const resp = await queryNoticePageApi({
      page: page.value,
      size: size.value,
      keyword: queryForm.keyword || undefined,
      readFlag:
        queryForm.readFlag === '' || queryForm.readFlag === undefined
          ? undefined
          : Number(queryForm.readFlag)
    });
    tableData.value = resp.data.records || [];
    total.value = Number(resp.data.total || 0);
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
  queryForm.readFlag = '';
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

function goDetail(noticeId: IdValue) {
  router.push(`/notice/detail/${noticeId}`);
}

function handleSelectionChange(rows: NoticeListItem[]) {
  selectedNoticeIds.value = rows.map((row) => row.noticeId);
}

async function handleMarkRead(noticeId: IdValue) {
  await noticeStore.markRead(noticeId);
  ElMessage.success('已标记为已读');
  fetchData();
}

async function handleDelete(noticeId: IdValue) {
  try {
    await ElMessageBox.confirm('确定删除这条消息吗？删除后将从消息中心移除。', '删除消息', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    });
  } catch {
    return;
  }
  await noticeStore.deleteNotices([noticeId]);
  ElMessage.success('已删除消息');
  await fetchData();
}

async function handleBatchDelete() {
  if (selectedNoticeIds.value.length <= 0) {
    return;
  }
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedNoticeIds.value.length} 条消息吗？`, '批量删除', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    });
  } catch {
    return;
  }
  await noticeStore.deleteNotices(selectedNoticeIds.value);
  selectedNoticeIds.value = [];
  ElMessage.success('已删除选中消息');
  await fetchData();
}

async function handleMarkAllRead() {
  if (noticeStore.unreadCount <= 0) {
    return;
  }
  await noticeStore.markAllRead();
  ElMessage.success('全部消息已标记为已读');
  fetchData();
}

onMounted(() => {
  if (!canViewNotice.value) {
    return;
  }
  noticeStore.refreshUnreadCount().catch(() => undefined);
  fetchData();
});
</script>

<style scoped lang="scss">
.notice-page {
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
  gap: 10px;
}

.title-wrap {
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
  display: flex;
  flex-wrap: wrap;
  gap: 12px 0;
}

.query-form__item {
  margin-right: 16px;
  margin-bottom: 0;
}

.query-form__item--keyword {
  width: 320px;
}

.query-form__item--status {
  width: 160px;
}

.query-form__item--actions {
  margin-right: 0;
}

:deep(.query-form__item--keyword .el-input) {
  width: 100%;
}

:deep(.query-form__item--status .el-select) {
  width: 100%;
}

.pager-wrap {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

@media (max-width: 768px) {
  .header-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .query-form__item {
    width: 100%;
    margin-right: 0;
  }

  .query-form__item--keyword,
  .query-form__item--status {
    width: 100%;
  }
}
</style>
