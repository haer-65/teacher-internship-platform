USE `teacher_internship_platform`;

SET @dept_admin_role_id = (
    SELECT `id`
    FROM `sys_role`
    WHERE `role_code` = 'DEPT_ADMIN' AND `deleted` = 0
    LIMIT 1
);

DELETE rm
FROM `sys_role_menu` rm
JOIN `sys_menu` m ON m.`id` = rm.`menu_id`
WHERE rm.`role_id` = @dept_admin_role_id
  AND rm.`deleted` = 0
  AND m.`permission_code` IN (
    'evaluation:view',
    'evaluation:submit'
  );
