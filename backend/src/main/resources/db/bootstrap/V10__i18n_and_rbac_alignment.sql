-- V10: 全站中文化补丁 + RBAC 角色边界修正（对齐立项/需求/详设）
USE `teacher_internship_platform`;

-- =========================================================
-- 1) 将历史英文种子数据（若存在）迁移为中文
-- =========================================================

-- 计划名称/说明中文化
UPDATE `biz_internship_plan`
SET `plan_name` = '2026年春季教育实习计划（草稿）'
WHERE `plan_name` = '2026 Spring Internship Plan (Draft)';

UPDATE `biz_internship_plan`
SET `description` = '用于模块联调的草稿计划'
WHERE `description` = 'Draft plan used for module integration tests';

UPDATE `biz_internship_plan`
SET `plan_name` = '2026年春季教育实习计划（已发布）'
WHERE `plan_name` = '2026 Spring Internship Plan (Published)';

UPDATE `biz_internship_plan`
SET `description` = '用于学生端查看与申请测试的已发布计划'
WHERE `description` = 'Published plan for student viewing and application tests';

UPDATE `biz_internship_plan`
SET `plan_name` = '2025年秋季教育实习计划（已结束）'
WHERE `plan_name` = '2025 Autumn Internship Plan (Finished)';

UPDATE `biz_internship_plan`
SET `description` = '用于状态流转测试的已结束计划'
WHERE `description` = 'Finished plan for status transition tests';

-- 学期中文化
UPDATE `biz_internship_plan` SET `term` = '春季' WHERE `term` = 'Spring';
UPDATE `biz_internship_plan` SET `term` = '秋季' WHERE `term` = 'Autumn';

-- 材料类型中文化
UPDATE `biz_material_type` SET `type_name` = '实习周志' WHERE `type_name` = 'Weekly Internship Log';
UPDATE `biz_material_type` SET `type_name` = '实习周志' WHERE `type_name` = 'Weekly Log';
UPDATE `biz_material_type` SET `type_name` = '中期报告' WHERE `type_name` = 'Midterm Report';
UPDATE `biz_material_type` SET `type_name` = '实习总结报告' WHERE `type_name` = 'Final Internship Report';
UPDATE `biz_material_type` SET `type_name` = '实习总结报告' WHERE `type_name` = 'Final Report';
UPDATE `biz_material_type` SET `type_name` = '实习教案' WHERE `type_name` = 'Lesson Plan';
UPDATE `biz_material_type` SET `type_name` = '课堂记录' WHERE `type_name` = 'Class Record';
UPDATE `biz_material_type` SET `type_name` = '课堂记录' WHERE `type_name` = 'Lesson Record';

-- 角色名称中文兜底
UPDATE `sys_role` SET `role_name` = '师范生' WHERE `role_code` = 'STUDENT' AND (`role_name` = 'Student' OR `role_name` = 'Normal Student');
UPDATE `sys_role` SET `role_name` = '校内指导教师' WHERE `role_code` = 'INNER_TEACHER' AND `role_name` = 'Inner Teacher';
UPDATE `sys_role` SET `role_name` = '实习基地指导教师' WHERE `role_code` = 'BASE_TEACHER' AND `role_name` = 'Base Teacher';
UPDATE `sys_role` SET `role_name` = '院系管理员' WHERE `role_code` = 'DEPT_ADMIN' AND `role_name` = 'Department Admin';
UPDATE `sys_role` SET `role_name` = '教务处管理员' WHERE `role_code` = 'ACADEMIC_ADMIN' AND `role_name` = 'Academic Admin';
UPDATE `sys_role` SET `role_name` = '系统管理员' WHERE `role_code` = 'SYS_ADMIN' AND `role_name` = 'System Admin';

-- 菜单中文兜底（按权限码映射）
UPDATE `sys_menu` SET `menu_name` = '工作台' WHERE `permission_code` = 'dashboard:view' AND `menu_type` = 'MENU';
UPDATE `sys_menu` SET `menu_name` = '实习计划' WHERE `permission_code` = 'plan:view' AND `menu_type` = 'MENU';
UPDATE `sys_menu` SET `menu_name` = '申请分配' WHERE `permission_code` = 'application:view' AND `menu_type` = 'MENU';
UPDATE `sys_menu` SET `menu_name` = '过程材料' WHERE `permission_code` = 'material:view' AND `menu_type` = 'MENU';
UPDATE `sys_menu` SET `menu_name` = '指导评价' WHERE `permission_code` = 'evaluation:view' AND `menu_type` = 'MENU';
UPDATE `sys_menu` SET `menu_name` = '成绩管理' WHERE `permission_code` = 'score:view' AND `menu_type` = 'MENU';
UPDATE `sys_menu` SET `menu_name` = '消息中心' WHERE `permission_code` = 'notice:view' AND `menu_type` = 'MENU';
UPDATE `sys_menu` SET `menu_name` = '统计分析' WHERE `permission_code` = 'stats:view' AND `menu_type` = 'MENU';
UPDATE `sys_menu` SET `menu_name` = '系统管理' WHERE `permission_code` = 'system:view' AND `menu_type` = 'MENU';
UPDATE `sys_menu` SET `menu_name` = '用户管理页面' WHERE `permission_code` = 'user:view' AND `menu_type` = 'MENU';

UPDATE `sys_menu` SET `menu_name` = '发布计划' WHERE `permission_code` = 'plan:publish' AND `menu_type` = 'BUTTON';
UPDATE `sys_menu` SET `menu_name` = '执行分配' WHERE `permission_code` = 'application:assign' AND `menu_type` = 'BUTTON';
UPDATE `sys_menu` SET `menu_name` = '上传材料' WHERE `permission_code` = 'material:upload' AND `menu_type` = 'BUTTON';
UPDATE `sys_menu` SET `menu_name` = '材料管理' WHERE `permission_code` = 'material:manage' AND `menu_type` = 'BUTTON';
UPDATE `sys_menu` SET `menu_name` = '提交评价' WHERE `permission_code` = 'evaluation:submit' AND `menu_type` = 'BUTTON';
UPDATE `sys_menu` SET `menu_name` = '发布成绩' WHERE `permission_code` = 'score:publish' AND `menu_type` = 'BUTTON';
UPDATE `sys_menu` SET `menu_name` = '调整成绩' WHERE `permission_code` = 'score:adjust' AND `menu_type` = 'BUTTON';
UPDATE `sys_menu` SET `menu_name` = '发送通知' WHERE `permission_code` = 'notice:send' AND `menu_type` = 'BUTTON';
UPDATE `sys_menu` SET `menu_name` = '导出报表' WHERE `permission_code` = 'stats:export' AND `menu_type` = 'BUTTON';
UPDATE `sys_menu` SET `menu_name` = '用户管理' WHERE `permission_code` = 'user:manage' AND `menu_type` = 'BUTTON';
UPDATE `sys_menu` SET `menu_name` = '角色管理' WHERE `permission_code` = 'role:manage' AND `menu_type` = 'BUTTON';
UPDATE `sys_menu` SET `menu_name` = '参数管理' WHERE `permission_code` = 'param:manage' AND `menu_type` = 'BUTTON';
UPDATE `sys_menu` SET `menu_name` = '个人信息' WHERE `permission_code` = 'auth:profile' AND `menu_type` = 'BUTTON';

-- =========================================================
-- 2) RBAC 边界对齐文档：系统管理员不参与业务评价/成绩/分配
-- =========================================================

DELETE rm
FROM `sys_role_menu` rm
JOIN `sys_menu` m ON m.`id` = rm.`menu_id`
WHERE rm.`role_id` = 1006
  AND m.`permission_code` IN (
    'plan:view', 'plan:publish',
    'application:view', 'application:assign',
    'material:view', 'material:upload', 'material:manage',
    'evaluation:view', 'evaluation:submit',
    'score:view', 'score:publish', 'score:adjust',
    'stats:view', 'stats:export'
  );
