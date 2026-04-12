-- V11: 系统管理员权限边界与系统管理能力补齐
USE `teacher_internship_platform`;

-- 系统管理员角色ID
SET @sys_admin_role_id = (
    SELECT `id`
    FROM `sys_role`
    WHERE `role_code` = 'SYS_ADMIN' AND `deleted` = 0
    LIMIT 1
);

-- 1) 清理系统管理员误分配的业务模块权限（与需求边界对齐）
DELETE rm
FROM `sys_role_menu` rm
JOIN `sys_menu` m ON m.`id` = rm.`menu_id`
WHERE rm.`role_id` = @sys_admin_role_id
  AND rm.`deleted` = 0
  AND m.`permission_code` IN (
    'plan:view', 'plan:publish',
    'application:view', 'application:assign',
    'material:view', 'material:upload', 'material:manage',
    'evaluation:view', 'evaluation:submit',
    'score:view', 'score:publish', 'score:adjust',
    'stats:view', 'stats:export'
  );

-- 2) 补齐系统管理员必须具备的系统管理能力
SET @next_role_menu_id = (SELECT IFNULL(MAX(`id`), 300100) FROM `sys_role_menu`);

INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_by`, `updated_by`, `deleted`)
SELECT
    (@next_role_menu_id := @next_role_menu_id + 1) AS `id`,
    @sys_admin_role_id AS `role_id`,
    m.`id` AS `menu_id`,
    0 AS `created_by`,
    0 AS `updated_by`,
    0 AS `deleted`
FROM `sys_menu` m
WHERE @sys_admin_role_id IS NOT NULL
  AND m.`deleted` = 0
  AND m.`permission_code` IN (
    'dashboard:view',
    'notice:view',
    'notice:send',
    'system:view',
    'user:view',
    'user:manage',
    'role:manage',
    'param:manage',
    'auth:profile'
  )
  AND NOT EXISTS (
    SELECT 1
    FROM `sys_role_menu` rm
    WHERE rm.`role_id` = @sys_admin_role_id
      AND rm.`menu_id` = m.`id`
      AND rm.`deleted` = 0
  );
