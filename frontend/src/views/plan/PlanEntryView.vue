<template>
  <div class="entry-wrap">
    <el-skeleton :rows="3" animated />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/modules/auth';

const router = useRouter();
const authStore = useAuthStore();

onMounted(() => {
  if (authStore.currentRoleCode === 'STUDENT') {
    router.replace('/plan/student');
    return;
  }
  router.replace('/plan/manage');
});
</script>

<style scoped lang="scss">
.entry-wrap {
  padding: 24px;
}
</style>
