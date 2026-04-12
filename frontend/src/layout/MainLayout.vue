<template>
  <div class="layout-shell">
    <el-container class="layout-container">
      <el-aside class="layout-aside" width="288px">
        <app-sidebar />
      </el-aside>
      <el-container>
        <el-header class="layout-header">
          <app-header />
        </el-header>
        <el-main class="layout-main">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import AppHeader from './components/AppHeader.vue';
import AppSidebar from './components/AppSidebar.vue';
</script>

<style scoped lang="scss">
.layout-shell,
.layout-container {
  width: 100%;
  height: 100%;
}

.layout-aside {
  background: #ffffff;
  color: #111827;
  border-right: 1px solid #e5e7eb;
}

.layout-header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  padding: 0 16px;
}

.layout-main {
  background: #f5f7fa;
  padding: 16px;
}
</style>
