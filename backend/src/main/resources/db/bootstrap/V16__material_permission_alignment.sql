USE `teacher_internship_platform`;

UPDATE `sys_menu`
SET `parent_id` = 2004,
    `menu_name` = '材料管理',
    `menu_type` = 'BUTTON',
    `route_path` = NULL,
    `component_path` = NULL,
    `icon` = NULL,
    `sort_no` = 2,
    `visible` = 1,
    `status` = 'ENABLED',
    `remark` = '材料管理按钮'
WHERE `permission_code` = 'material:manage'
  AND `deleted` = 0;

INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`, `created_by`, `updated_by`, `deleted`)
SELECT 2113, 2004, '材料管理', 'BUTTON', NULL, NULL, 'material:manage', NULL, 2, 1, 'ENABLED', '材料管理按钮', 0, 0, 0
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_menu`
    WHERE `permission_code` = 'material:manage'
      AND `deleted` = 0
);

SET @dept_admin_role_id = (
    SELECT `id`
    FROM `sys_role`
    WHERE `role_code` = 'DEPT_ADMIN' AND `deleted` = 0
    LIMIT 1
);

SET @sys_admin_role_id = (
    SELECT `id`
    FROM `sys_role`
    WHERE `role_code` = 'SYS_ADMIN' AND `deleted` = 0
    LIMIT 1
);

DELETE rm
FROM `sys_role_menu` rm
JOIN `sys_menu` m ON m.`id` = rm.`menu_id`
WHERE rm.`deleted` = 0
  AND (
      (rm.`role_id` = @dept_admin_role_id AND m.`permission_code` = 'material:upload')
      OR (rm.`role_id` = @sys_admin_role_id AND m.`permission_code` IN ('material:view', 'material:upload', 'material:manage'))
  );

SET @next_role_menu_id = (SELECT IFNULL(MAX(`id`), 300100) FROM `sys_role_menu`);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_by`, `updated_by`, `deleted`)
SELECT
    (@next_role_menu_id := @next_role_menu_id + 1) AS `id`,
    @dept_admin_role_id AS `role_id`,
    m.`id` AS `menu_id`,
    0 AS `created_by`,
    0 AS `updated_by`,
    0 AS `deleted`
FROM `sys_menu` m
WHERE @dept_admin_role_id IS NOT NULL
  AND m.`deleted` = 0
  AND m.`permission_code` = 'material:manage'
  AND NOT EXISTS (
      SELECT 1
      FROM `sys_role_menu` rm
      WHERE rm.`role_id` = @dept_admin_role_id
        AND rm.`menu_id` = m.`id`
        AND rm.`deleted` = 0
  );
