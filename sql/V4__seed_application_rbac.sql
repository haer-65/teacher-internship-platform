-- =============================================
-- 申请分配模块 RBAC 补充数据
-- =============================================

USE `teacher_internship_platform`;

-- 允许校内导师/基地导师查看“申请分配”菜单
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_by`, `updated_by`)
SELECT 300066, 1002, 2003, 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu`
    WHERE `role_id` = 1002 AND `menu_id` = 2003 AND `deleted` = 0
);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_by`, `updated_by`)
SELECT 300067, 1003, 2003, 0, 0
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_role_menu`
    WHERE `role_id` = 1003 AND `menu_id` = 2003 AND `deleted` = 0
);
