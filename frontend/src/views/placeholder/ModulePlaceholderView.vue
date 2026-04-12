<template>
  <el-card>
    <template #header>
      <div class="title">{{ pageTitle }}</div>
    </template>
    <p>该页面为当前模块预留页。</p>
    <p>认证与权限基础能力已就绪，本模块将按迭代计划持续完善。</p>
  </el-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';

const route = useRoute();
const pageTitle = computed(() => (route.meta.title as string) || '模块');
</script>

<style scoped>
.title {
  font-weight: 600;
}
</style>
