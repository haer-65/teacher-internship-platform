<template>
  <el-card class="page-card">
    <template #header>
      <div class="header-row">
        <span class="page-title">基础数据维护</span>
        <span class="page-desc">维护院系、专业、年级、实习基地等基础台账。</span>
      </div>
    </template>

    <system-base-data-tab />
  </el-card>
</template>

<script setup lang="ts">
import SystemBaseDataTab from '@/views/system/components/SystemBaseDataTab.vue';
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
