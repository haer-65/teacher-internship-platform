<template>
  <el-card class="page-card">
    <template #header>
      <div class="header-row">
        <span class="page-title">日志审计</span>
        <span class="page-desc">查看登录日志与操作日志，支持按条件检索。</span>
      </div>
    </template>

    <system-log-tab />
  </el-card>
</template>

<script setup lang="ts">
import SystemLogTab from '@/views/system/components/SystemLogTab.vue';
</script>

<style scoped lang="scss">
.page-card {
  min-height: 100%;
}

.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.page-title {
  font-size: 16px;
  font-weight: 600;
}

.page-desc {
  color: #909399;
  font-size: 13px;
}
</style>
