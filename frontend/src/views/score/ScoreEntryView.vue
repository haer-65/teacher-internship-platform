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
    router.replace('/score/student');
    return;
  }
  if (roleCode === 'DEPT_ADMIN' || roleCode === 'SYS_ADMIN') {
    router.replace('/score/list');
    return;
  }
  if (roleCode === 'INNER_TEACHER' || roleCode === 'BASE_TEACHER' || roleCode === 'ACADEMIC_ADMIN') {
    router.replace('/score/list');
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
