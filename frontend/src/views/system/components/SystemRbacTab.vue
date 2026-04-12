<template>
  <el-row :gutter="12">
    <el-col :xs="24" :md="8">
      <el-card shadow="never" class="rbac-card">
        <template #header>
          <span class="title">角色列表</span>
        </template>
        <el-skeleton v-if="loading.roles" :rows="6" animated />
        <el-empty v-else-if="roles.length === 0" description="暂无角色数据" />
        <el-radio-group v-else v-model="selectedRoleId" class="role-group" @change="handleRoleChange">
          <el-radio v-for="item in roles" :key="item.id" :label="item.id" class="role-item">
            {{ item.roleName }} ({{ item.roleCode }})
          </el-radio>
        </el-radio-group>
      </el-card>
    </el-col>

    <el-col :xs="24" :md="16">
      <el-card shadow="never" class="rbac-card">
        <template #header>
          <div class="header-row">
            <span class="title">菜单权限树</span>
            <el-button
              type="primary"
              :disabled="!selectedRoleId || loading.tree"
              :loading="saving"
              @click="saveRoleMenus"
            >
              保存配置
            </el-button>
          </div>
        </template>

        <el-input
          v-model.trim="menuKeyword"
          clearable
          class="filter-input"
          placeholder="按菜单名/权限码/路由筛选"
        />

        <el-skeleton v-if="loading.tree" :rows="8" animated />
        <el-empty v-else-if="!selectedRoleId" description="请先选择角色" />
        <el-tree
          v-else
          ref="treeRef"
          :data="menuTree"
          node-key="id"
          show-checkbox
          :default-expand-all="true"
          :props="treeProps"
          :filter-node-method="filterNode"
          class="menu-tree"
        >
          <template #default="{ data }">
            <span class="menu-node">
              <span>{{ data.menuName }}</span>
              <span class="menu-meta">
                {{ menuTypeLabel(data.menuType) }}
                <template v-if="data.permissionCode"> / {{ data.permissionCode }}</template>
              </span>
            </span>
          </template>
        </el-tree>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { nextTick, onMounted, reactive, ref, watch } from 'vue';
import { ElMessage } from 'element-plus';
import type { TreeInstance } from 'element-plus';
import {
  assignRoleMenusApi,
  queryMenuTreeApi,
  queryRoleListApi
} from '@/api/modules/system-admin';
import type { SysMenuNode, SysRoleItem } from '@/types/api';

const roles = ref<SysRoleItem[]>([]);
const selectedRoleId = ref<number>();
const menuTree = ref<SysMenuNode[]>([]);
const menuKeyword = ref('');
const treeRef = ref<TreeInstance>();
const saving = ref(false);
const loading = reactive({
  roles: false,
  tree: false
});

const treeProps = {
  label: 'menuName',
  children: 'children'
};

function collectCheckedMenuIds(nodes: SysMenuNode[], bucket: number[] = []): number[] {
  nodes.forEach((node) => {
    if (node.selected) {
      bucket.push(node.id);
    }
    if (node.children?.length) {
      collectCheckedMenuIds(node.children, bucket);
    }
  });
  return bucket;
}

async function fetchRoles() {
  loading.roles = true;
  try {
    const resp = await queryRoleListApi();
    roles.value = resp.data || [];
    if (!selectedRoleId.value && roles.value.length > 0) {
      selectedRoleId.value = roles.value[0].id;
      await fetchRoleMenus();
    }
  } finally {
    loading.roles = false;
  }
}

async function fetchRoleMenus() {
  if (!selectedRoleId.value) {
    menuTree.value = [];
    return;
  }
  loading.tree = true;
  try {
    const treeResp = await queryMenuTreeApi(selectedRoleId.value);
    menuTree.value = treeResp.data || [];
  } finally {
    loading.tree = false;
    await nextTick();
    treeRef.value?.setCheckedKeys(collectCheckedMenuIds(menuTree.value));
  }
}

async function handleRoleChange() {
  await fetchRoleMenus();
}

async function saveRoleMenus() {
  if (!selectedRoleId.value) {
    ElMessage.warning('请先选择角色');
    return;
  }
  const checked = (treeRef.value?.getCheckedKeys(false) || []) as Array<number | string>;
  const halfChecked = (treeRef.value?.getHalfCheckedKeys() || []) as Array<number | string>;
  const menuIds = Array.from(
    new Set([...checked, ...halfChecked].map((item) => Number(item)).filter((item) => !Number.isNaN(item)))
  );

  saving.value = true;
  try {
    await assignRoleMenusApi(selectedRoleId.value, { menuIds });
    ElMessage.success('角色菜单权限更新成功');
    await fetchRoleMenus();
  } finally {
    saving.value = false;
  }
}

function filterNode(keyword: string, data: SysMenuNode): boolean {
  if (!keyword) {
    return true;
  }
  const value = keyword.toLowerCase();
  return [data.menuName, data.permissionCode, data.routePath]
    .filter((item): item is string => !!item)
    .some((item) => item.toLowerCase().includes(value));
}

function menuTypeLabel(menuType?: string): string {
  if (menuType === 'MENU') {
    return '菜单';
  }
  if (menuType === 'BUTTON') {
    return '按钮';
  }
  return menuType || '-';
}

watch(menuKeyword, (value) => {
  treeRef.value?.filter(value);
});

onMounted(() => {
  fetchRoles();
});
</script>

<style scoped lang="scss">
.rbac-card {
  min-height: 540px;
}

.title {
  font-weight: 600;
}

.header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.role-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.role-item {
  margin-right: 0;
}

.filter-input {
  margin-bottom: 12px;
}

.menu-tree {
  max-height: 430px;
  overflow: auto;
}

.menu-node {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.menu-meta {
  color: #909399;
  font-size: 12px;
}
</style>
