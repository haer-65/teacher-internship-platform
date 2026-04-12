package com.teacher.internship.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.system.entity.SysMenu;
import com.teacher.internship.modules.system.entity.SysRole;
import com.teacher.internship.modules.system.entity.SysRoleMenu;
import com.teacher.internship.modules.system.mapper.SysMenuMapper;
import com.teacher.internship.modules.system.mapper.SysRoleMapper;
import com.teacher.internship.modules.system.mapper.SysRoleMenuMapper;
import com.teacher.internship.modules.system.vo.SysMenuNodeVO;
import com.teacher.internship.modules.system.vo.SysRoleItemVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
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
public class SystemRbacAdminService {

    private final SysRoleMapper roleMapper;
    private final SysMenuMapper menuMapper;
    private final SysRoleMenuMapper roleMenuMapper;

    public SystemRbacAdminService(SysRoleMapper roleMapper,
                                  SysMenuMapper menuMapper,
                                  SysRoleMenuMapper roleMenuMapper) {
        this.roleMapper = roleMapper;
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    public List<SysRoleItemVO> queryRoleList() {
        List<SysRole> roles = roleMapper.selectList(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getDeleted, 0L)
                .orderByAsc(SysRole::getId));
        return roles.stream().map(this::toRoleVO).collect(Collectors.toList());
    }

    public List<Long> queryRoleMenuIds(Long roleId) {
        requireRole(roleId);
        return roleMenuMapper.selectList(new LambdaQueryWrapper<SysRoleMenu>()
                        .eq(SysRoleMenu::getRoleId, roleId)
                        .eq(SysRoleMenu::getDeleted, 0L))
                .stream()
                .map(SysRoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<SysMenuNodeVO> queryMenuTree(Long roleId) {
        Set<Long> selectedIds = new HashSet<>();
        if (roleId != null) {
            selectedIds.addAll(queryRoleMenuIds(roleId));
        }

        List<SysMenu> menuList = menuMapper.selectList(new LambdaQueryWrapper<SysMenu>()
                .eq(SysMenu::getDeleted, 0L)
                .orderByAsc(SysMenu::getSortNo)
                .orderByAsc(SysMenu::getId));
        Map<Long, SysMenuNodeVO> nodeMap = new HashMap<>();
        for (SysMenu menu : menuList) {
            SysMenuNodeVO node = toMenuNodeVO(menu);
            node.setSelected(selectedIds.contains(menu.getId()));
            nodeMap.put(menu.getId(), node);
        }

        List<SysMenuNodeVO> roots = new ArrayList<>();
        for (SysMenuNodeVO node : nodeMap.values()) {
            if (node.getParentId() == null || node.getParentId() == 0L || !nodeMap.containsKey(node.getParentId())) {
                roots.add(node);
            } else {
                nodeMap.get(node.getParentId()).getChildren().add(node);
            }
        }
        roots.sort(Comparator.comparing(item -> item.getSortNo() == null ? 0 : item.getSortNo()));
        roots.forEach(this::sortChildren);
        return roots;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> assignRoleMenus(Long roleId, List<Long> menuIds, Long operatorId) {
        SysRole role = requireRole(roleId);
        if (!"ENABLED".equalsIgnoreCase(role.getStatus())) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "角色已禁用，不能配置权限");
        }

        Set<Long> targetMenuIds = menuIds == null ? new HashSet<>() : menuIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(HashSet::new));
        if (!CollectionUtils.isEmpty(targetMenuIds)) {
            long validCount = menuMapper.selectCount(new LambdaQueryWrapper<SysMenu>()
                    .in(SysMenu::getId, targetMenuIds)
                    .eq(SysMenu::getDeleted, 0L));
            if (validCount != targetMenuIds.size()) {
                throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "存在无效的菜单ID");
            }
        }

        long deletedValue = System.currentTimeMillis();
        LocalDateTime now = LocalDateTime.now();
        roleMenuMapper.update(null, new LambdaUpdateWrapper<SysRoleMenu>()
                .eq(SysRoleMenu::getRoleId, roleId)
                .eq(SysRoleMenu::getDeleted, 0L)
                .set(SysRoleMenu::getDeleted, deletedValue)
                .set(SysRoleMenu::getUpdatedBy, operatorId)
                .set(SysRoleMenu::getUpdatedTime, now));

        for (Long menuId : targetMenuIds) {
            SysRoleMenu relation = new SysRoleMenu();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            relation.setCreatedBy(operatorId);
            relation.setUpdatedBy(operatorId);
            relation.setDeleted(0L);
            roleMenuMapper.insert(relation);
        }
        return queryRoleMenuIds(roleId);
    }

    private SysRole requireRole(Long roleId) {
        if (roleId == null) {
            throw new BusinessException(ApiCode.BAD_REQUEST.getCode(), "角色标识不能为空");
        }
        SysRole role = roleMapper.selectOne(new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getId, roleId)
                .eq(SysRole::getDeleted, 0L)
                .last("LIMIT 1"));
        if (role == null) {
            throw new BusinessException(ApiCode.NOT_FOUND.getCode(), "角色不存在");
        }
        return role;
    }

    private SysRoleItemVO toRoleVO(SysRole role) {
        SysRoleItemVO vo = new SysRoleItemVO();
        vo.setId(role.getId());
        vo.setRoleCode(role.getRoleCode());
        vo.setRoleName(role.getRoleName());
        vo.setDataScope(role.getDataScope());
        vo.setStatus(role.getStatus());
        vo.setRemark(role.getRemark());
        return vo;
    }

    private SysMenuNodeVO toMenuNodeVO(SysMenu menu) {
        SysMenuNodeVO vo = new SysMenuNodeVO();
        vo.setId(menu.getId());
        vo.setParentId(menu.getParentId());
        vo.setMenuName(menu.getMenuName());
        vo.setMenuType(menu.getMenuType());
        vo.setRoutePath(menu.getRoutePath());
        vo.setComponentPath(menu.getComponentPath());
        vo.setPermissionCode(menu.getPermissionCode());
        vo.setIcon(menu.getIcon());
        vo.setSortNo(menu.getSortNo());
        vo.setVisible(menu.getVisible());
        vo.setStatus(menu.getStatus());
        return vo;
    }

    private void sortChildren(SysMenuNodeVO node) {
        if (CollectionUtils.isEmpty(node.getChildren())) {
            return;
        }
        node.getChildren().sort(Comparator.comparing(item -> item.getSortNo() == null ? 0 : item.getSortNo()));
        node.getChildren().forEach(this::sortChildren);
    }
}
