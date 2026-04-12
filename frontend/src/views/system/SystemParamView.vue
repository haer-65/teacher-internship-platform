<template>
  <el-card class="page-card">
    <template #header>
      <div class="header-row">
        <span class="page-title">系统参数</span>
        <span class="page-desc">维护文件大小、通知提前天数、默认业务参数等配置。</span>
      </div>
    </template>

    <system-param-tab />
  </el-card>
</template>

<script setup lang="ts">
import SystemParamTab from '@/views/system/components/SystemParamTab.vue';
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
