-- V12: 补齐系统管理菜单结构，统一系统管理员入口
USE `teacher_internship_platform`;

UPDATE `sys_menu`
SET `menu_name` = '用户管理',
    `remark` = '用户管理菜单'
WHERE `id` = 2010
  AND `deleted` = 0;

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`, `created_by`, `updated_by`, `deleted`)
SELECT 2011, 2009, '基础数据维护', 'MENU', '/system/base', 'views/system/SystemBaseDataView', NULL, 'OfficeBuilding', 2, 1, 'ENABLED', '基础数据维护菜单', 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2011);

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`, `created_by`, `updated_by`, `deleted`)
SELECT 2012, 2009, '系统参数', 'MENU', '/system/params', 'views/system/SystemParamView', NULL, 'Tools', 3, 1, 'ENABLED', '系统参数菜单', 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2012);

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`, `created_by`, `updated_by`, `deleted`)
SELECT 2013, 2009, '日志审计', 'MENU', '/system/logs', 'views/system/SystemLogView', NULL, 'Document', 4, 1, 'ENABLED', '日志审计菜单', 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2013);

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`, `created_by`, `updated_by`, `deleted`)
SELECT 2014, 2009, '角色权限配置', 'MENU', '/system/rbac', 'views/system/SystemRbacView', NULL, 'Lock', 5, 1, 'ENABLED', '角色权限配置菜单', 0, 0, 0
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 2014);

SET @sys_admin_role_id = (
    SELECT `id`
    FROM `sys_role`
    WHERE `role_code` = 'SYS_ADMIN' AND `deleted` = 0
    LIMIT 1
);

SET @next_role_menu_id = (SELECT IFNULL(MAX(`id`), 300200) FROM `sys_role_menu`);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_by`, `updated_by`, `deleted`)
SELECT
    (@next_role_menu_id := @next_role_menu_id + 1) AS `id`,
    @sys_admin_role_id AS `role_id`,
    target_menu.`id` AS `menu_id`,
    0 AS `created_by`,
    0 AS `updated_by`,
    0 AS `deleted`
FROM `sys_menu` target_menu
WHERE @sys_admin_role_id IS NOT NULL
  AND target_menu.`id` IN (2010, 2011, 2012, 2013, 2014)
  AND target_menu.`deleted` = 0
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.`role_id` = @sys_admin_role_id
      AND rm.`menu_id` = target_menu.`id`
      AND rm.`deleted` = 0
  );
