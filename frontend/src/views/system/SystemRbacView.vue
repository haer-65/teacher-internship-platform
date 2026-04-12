<template>
  <el-card class="page-card">
    <template #header>
      <div class="header-row">
        <span class="page-title">角色权限配置</span>
        <span class="page-desc">为各角色配置菜单权限，调整后立即影响系统访问范围。</span>
      </div>
    </template>

    <system-rbac-tab />
  </el-card>
</template>

<script setup lang="ts">
import SystemRbacTab from '@/views/system/components/SystemRbacTab.vue';
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
