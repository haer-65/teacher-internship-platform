<template>
  <el-sub-menu v-if="hasChildren" :index="item.routePath || String(item.id)">
    <template #title>
      <div class="menu-label">
        <span class="menu-icon">
          <el-icon><component :is="menuIcon" /></el-icon>
        </span>
        <span class="menu-text">{{ item.menuName }}</span>
      </div>
    </template>
    <sidebar-menu-item v-for="child in item.children" :key="child.id" :item="child" />
  </el-sub-menu>
  <el-menu-item v-else :index="item.routePath || '/dashboard'">
    <div class="menu-label">
      <span class="menu-icon">
        <el-icon><component :is="menuIcon" /></el-icon>
      </span>
      <span class="menu-text">{{ item.menuName }}</span>
    </div>
  </el-menu-item>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import {
  Bell,
  Calendar,
  Collection,
  DataAnalysis,
  Document,
  Fold,
  HomeFilled,
  Lock,
  Memo,
  Notebook,
  Notification,
  Operation,
  Opportunity,
  Postcard,
  Reading,
  School,
  Search,
  Setting,
  User,
  UserFilled,
  Wallet
} from '@element-plus/icons-vue';
import type { MenuNode } from '@/types/api';

const props = defineProps<{
  item: MenuNode;
}>();

const hasChildren = computed(() => props.item.children && props.item.children.length > 0);

const routeIconMap: Record<string, unknown> = {
  '/dashboard': HomeFilled,
  '/notice': Bell,
  '/plan': Calendar,
  '/application': Postcard,
  '/material': Notebook,
  '/evaluation': Reading,
  '/score': Opportunity,
  '/stats': DataAnalysis,
  '/system': Setting,
  '/system/users': UserFilled,
  '/system/base-data': Collection,
  '/system/params': Operation,
  '/system/logs': Memo,
  '/system/rbac': Lock
};

const keywordIconMap: Array<[string, unknown]> = [
  ['工作台', HomeFilled],
  ['消息中心', Bell],
  ['通知', Notification],
  ['计划', Calendar],
  ['申请', Postcard],
  ['分配', Fold],
  ['材料', Notebook],
  ['评价', Reading],
  ['成绩', Wallet],
  ['统计', DataAnalysis],
  ['系统管理', Setting],
  ['用户管理', UserFilled],
  ['用户', User],
  ['基础数据维护', Collection],
  ['基础数据', Collection],
  ['参数', Operation],
  ['日志审计', Memo],
  ['日志', Memo],
  ['角色权限配置', Lock],
  ['角色', Lock],
  ['权限', Lock],
  ['基地', School],
  ['search', Search],
  ['搜索', Search]
];

const menuIcon = computed(() => {
  const routePath = props.item.routePath || '';
  const exactMatch = routeIconMap[routePath];

  if (exactMatch) {
    return exactMatch;
  }

  const routeMatch = Object.entries(routeIconMap)
    .sort((left, right) => right[0].length - left[0].length)
    .find(([key]) => key !== '/' && routePath.startsWith(key));

  if (routeMatch) {
    return routeMatch[1];
  }

  const source = `${props.item.menuName || ''} ${routePath}`.toLowerCase();
  const keywordMatch = keywordIconMap.find(([keyword]) => source.includes(keyword.toLowerCase()));

  return keywordMatch?.[1] || Document;
});
</script>

<style scoped lang="scss">
.menu-label {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

.menu-icon {
  width: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  color: #111827;
  font-size: 18px;
  flex-shrink: 0;
}

.menu-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

:deep(.el-sub-menu__icon-arrow) {
  margin-top: -1px;
  right: 10px;
  color: #6b7280;
}

:deep(.el-sub-menu .el-menu-item) {
  padding-left: 32px !important;
}
</style>
