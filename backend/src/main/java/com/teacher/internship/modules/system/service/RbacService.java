package com.teacher.internship.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.internship.modules.auth.vo.MenuTreeVO;
import com.teacher.internship.modules.auth.vo.RoleOptionVO;
import com.teacher.internship.modules.system.entity.SysMenu;
import com.teacher.internship.modules.system.entity.SysRole;
import com.teacher.internship.modules.system.entity.SysRoleMenu;
import com.teacher.internship.modules.system.entity.SysUserRole;
import com.teacher.internship.modules.system.mapper.SysMenuMapper;
import com.teacher.internship.modules.system.mapper.SysRoleMapper;
import com.teacher.internship.modules.system.mapper.SysRoleMenuMapper;
import com.teacher.internship.modules.system.mapper.SysUserRoleMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RbacService {

    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final SysRoleMenuMapper sysRoleMenuMapper;
    private final SysMenuMapper sysMenuMapper;

    public RbacService(SysUserRoleMapper sysUserRoleMapper,
                       SysRoleMapper sysRoleMapper,
                       SysRoleMenuMapper sysRoleMenuMapper,
                       SysMenuMapper sysMenuMapper) {
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.sysRoleMenuMapper = sysRoleMenuMapper;
        this.sysMenuMapper = sysMenuMapper;
    }

    public List<SysRole> getEnabledRolesByUserId(Long userId) {
        if (userId == null) {
            return new ArrayList<>();
        }

        List<SysUserRole> relations = sysUserRoleMapper.selectList(
                new LambdaQueryWrapper<SysUserRole>()
                        .eq(SysUserRole::getUserId, userId)
                        .eq(SysUserRole::getDeleted, 0L)
        );
        if (CollectionUtils.isEmpty(relations)) {
            return new ArrayList<>();
        }

        Set<Long> roleIds = relations.stream()
                .map(SysUserRole::getRoleId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(roleIds)) {
            return new ArrayList<>();
        }

        return sysRoleMapper.selectBatchIds(roleIds).stream()
                .filter(Objects::nonNull)
                .filter(role -> role.getDeleted() != null && role.getDeleted() == 0L)
                .filter(role -> "ENABLED".equals(role.getStatus()))
                .sorted(Comparator.comparing(SysRole::getId))
                .collect(Collectors.toList());
    }

    public List<RoleOptionVO> getRoleOptionsByUserId(Long userId) {
        return getEnabledRolesByUserId(userId).stream()
                .map(role -> new RoleOptionVO(role.getRoleCode(), role.getRoleName()))
                .collect(Collectors.toList());
    }

    public boolean userHasRoleCode(Long userId, String roleCode) {
        if (userId == null || !StringUtils.hasText(roleCode)) {
            return false;
        }
        return getEnabledRolesByUserId(userId).stream().anyMatch(role -> roleCode.equals(role.getRoleCode()));
    }

    public Set<String> getPermissionCodesByRoleCode(String roleCode) {
        List<SysMenu> menus = getMenusByRoleCode(roleCode);
        return menus.stream()
                .map(SysMenu::getPermissionCode)
                .filter(StringUtils::hasText)
                .collect(Collectors.toCollection(HashSet::new));
    }

    public List<MenuTreeVO> getMenuTreeByRoleCode(String roleCode) {
        List<SysMenu> menuEntities = getMenusByRoleCode(roleCode).stream()
                .filter(menu -> "MENU".equals(menu.getMenuType()))
                .filter(menu -> menu.getVisible() == null || menu.getVisible() == 1)
                .sorted(Comparator.comparing(menu -> menu.getSortNo() == null ? 0 : menu.getSortNo()))
                .collect(Collectors.toList());

        Map<Long, MenuTreeVO> nodeMap = new HashMap<>();
        for (SysMenu entity : menuEntities) {
            MenuTreeVO node = new MenuTreeVO();
            node.setId(entity.getId());
            node.setParentId(entity.getParentId());
            node.setMenuName(entity.getMenuName());
            node.setRoutePath(entity.getRoutePath());
            node.setComponentPath(entity.getComponentPath());
            node.setIcon(entity.getIcon());
            node.setSortNo(entity.getSortNo());
            nodeMap.put(entity.getId(), node);
        }

        List<MenuTreeVO> roots = new ArrayList<>();
        for (MenuTreeVO node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId() == 0 || !nodeMap.containsKey(node.getParentId())) {
                roots.add(node);
                continue;
            }
            nodeMap.get(node.getParentId()).getChildren().add(node);
        }

        roots.sort(Comparator.comparing(node -> node.getSortNo() == null ? 0 : node.getSortNo()));
        for (MenuTreeVO root : roots) {
            sortChildren(root);
        }
        return roots;
    }

    private void sortChildren(MenuTreeVO node) {
        if (CollectionUtils.isEmpty(node.getChildren())) {
            return;
        }
        node.getChildren().sort(Comparator.comparing(child -> child.getSortNo() == null ? 0 : child.getSortNo()));
        node.getChildren().forEach(this::sortChildren);
    }

    private List<SysMenu> getMenusByRoleCode(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            return new ArrayList<>();
        }

        SysRole role = sysRoleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
                .eq(SysRole::getStatus, "ENABLED")
                .eq(SysRole::getDeleted, 0L)
                .last("LIMIT 1"));
        if (role == null) {
            return new ArrayList<>();
        }

        List<SysRoleMenu> roleMenus = sysRoleMenuMapper.selectList(
                new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, role.getId())
                        .eq(SysRoleMenu::getDeleted, 0L)
        );
        if (CollectionUtils.isEmpty(roleMenus)) {
            return new ArrayList<>();
        }

        Set<Long> menuIds = roleMenus.stream()
                .map(SysRoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(menuIds)) {
            return new ArrayList<>();
        }

        return sysMenuMapper.selectBatchIds(menuIds).stream()
                .filter(Objects::nonNull)
                .filter(menu -> menu.getDeleted() != null && menu.getDeleted() == 0L)
                .filter(menu -> "ENABLED".equals(menu.getStatus()))
                .collect(Collectors.toList());
    }
}

