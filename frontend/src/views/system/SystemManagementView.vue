<template>
  <div class="system-overview">
    <el-card class="hero-card" shadow="never">
      <div class="hero-title">系统管理</div>
      <div class="hero-desc">集中维护平台账号、角色权限、基础数据、系统参数和日志审计能力，保障整个平台配置统一生效。</div>
    </el-card>

    <el-row :gutter="16">
      <el-col v-for="item in accessModules" :key="item.path" :xs="24" :sm="12" :lg="8">
        <el-card class="module-card" shadow="hover" @click="goModule(item.path)">
          <div class="module-title">{{ item.title }}</div>
          <div class="module-desc">{{ item.description }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-alert v-if="accessModules.length === 0" title="当前角色暂无系统管理访问权限" type="warning" :closable="false" show-icon />
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/store/modules/auth';

interface ModuleCard {
  title: string;
  description: string;
  path: string;
  requiredPermission: string;
}

const router = useRouter();
const authStore = useAuthStore();

const moduleCards: ModuleCard[] = [
  {
    title: '用户管理页面',
    description: '维护平台用户、状态控制、密码重置和批量导入。',
    path: '/system/users',
    requiredPermission: 'user:view'
  },
  {
    title: '基础数据维护',
    description: '维护院系、专业、年级、实习基地等基础资料。',
    path: '/system/base',
    requiredPermission: 'user:manage'
  },
  {
    title: '系统参数',
    description: '统一配置文件大小、提醒天数和默认业务参数。',
    path: '/system/params',
    requiredPermission: 'param:manage'
  },
  {
    title: '日志审计',
    description: '按条件查询登录日志与操作日志，满足审计追溯。',
    path: '/system/logs',
    requiredPermission: 'system:view'
  },
  {
    title: '角色权限配置',
    description: '配置角色菜单和权限码，严格控制角色访问边界。',
    path: '/system/rbac',
    requiredPermission: 'role:manage'
  }
];

const accessModules = computed(() =>
  moduleCards.filter((item) => authStore.permissionCodes.includes(item.requiredPermission))
);

function goModule(path: string) {
  router.push(path);
}
</script>

<style scoped lang="scss">
.system-overview {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.hero-card {
  border: none;
  background: linear-gradient(135deg, #1d4ed8, #3b82f6);
  color: #fff;
}

.hero-title {
  font-size: 24px;
  font-weight: 700;
}

.hero-desc {
  margin-top: 10px;
  line-height: 1.8;
}

.module-card {
  cursor: pointer;
  min-height: 160px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.module-card:hover {
  transform: translateY(-2px);
}

.module-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2329;
}

.module-desc {
  margin-top: 12px;
  color: #5c6675;
  line-height: 1.7;
}
</style>
