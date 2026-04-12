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
  const roleCode = authStore.currentRoleCode;
  if (roleCode === 'STUDENT') {
    router.replace('/evaluation/student');
    return;
  }
  if (roleCode === 'INNER_TEACHER' || roleCode === 'BASE_TEACHER') {
    router.replace('/evaluation/teacher/pending');
    return;
  }
  router.replace('/dashboard');
});
</script>

<style scoped lang="scss">
.entry-wrap {
  padding: 24px;
}
</style>
