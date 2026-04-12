USE `teacher_internship_platform`;

UPDATE `biz_internship_plan`
SET `plan_name` = '2026年春季教育实习计划（草稿）',
    `description` = '用于模块联调的草稿计划',
    `term` = '春季'
WHERE `id` = 7001;

UPDATE `biz_internship_plan`
SET `plan_name` = '2026年春季教育实习计划（已发布）',
    `description` = '用于学生查看与申请测试的已发布计划',
    `term` = '春季'
WHERE `id` = 7002;

UPDATE `biz_internship_plan`
SET `plan_name` = '2025年秋季教育实习计划（已结束）',
    `description` = '用于状态流转测试的已结束计划',
    `term` = '秋季'
WHERE `id` = 7003;

UPDATE `biz_plan_base`
SET `remark` = CASE
    WHEN `plan_id` = 7001 THEN '草稿计划示例基地'
    WHEN `plan_id` = 7002 THEN '已发布计划示例基地'
    WHEN `plan_id` = 7003 THEN '历史计划示例基地'
    ELSE `remark`
END
WHERE `plan_id` IN (7001, 7002, 7003)
  AND `deleted` = 0;

UPDATE `biz_material_type`
SET `type_name` = CASE `type_code`
    WHEN 'WEEKLY_LOG' THEN '实习周志'
    WHEN 'MID_REPORT' THEN '中期报告'
    WHEN 'FINAL_REPORT' THEN '实习总结报告'
    WHEN 'LESSON_PLAN' THEN '实习教案'
    WHEN 'LESSON_RECORD' THEN '课堂记录'
    ELSE `type_name`
END
WHERE `plan_id` IN (7001, 7002, 7003)
  AND `deleted` = 0;

UPDATE `sys_role`
SET `role_name` = CASE `role_code`
    WHEN 'STUDENT' THEN '师范生'
    WHEN 'INNER_TEACHER' THEN '校内指导教师'
    WHEN 'BASE_TEACHER' THEN '基地指导教师'
    WHEN 'DEPT_ADMIN' THEN '院系管理员'
    WHEN 'ACADEMIC_ADMIN' THEN '教务处管理员'
    WHEN 'SYS_ADMIN' THEN '系统管理员'
    ELSE `role_name`
END
WHERE `role_code` IN ('STUDENT', 'INNER_TEACHER', 'BASE_TEACHER', 'DEPT_ADMIN', 'ACADEMIC_ADMIN', 'SYS_ADMIN')
  AND `deleted` = 0;

UPDATE `sys_menu`
SET `menu_name` = CASE
    WHEN `permission_code` = 'dashboard:view' THEN '工作台'
    WHEN `permission_code` = 'plan:view' AND `menu_type` = 'MENU' THEN '实习计划'
    WHEN `permission_code` = 'application:view' AND `menu_type` = 'MENU' THEN '申请分配'
    WHEN `permission_code` = 'material:view' AND `menu_type` = 'MENU' THEN '过程材料'
    WHEN `permission_code` = 'evaluation:view' AND `menu_type` = 'MENU' THEN '指导评价'
    WHEN `permission_code` = 'score:view' AND `menu_type` = 'MENU' THEN '成绩管理'
    WHEN `permission_code` = 'notice:view' AND `menu_type` = 'MENU' THEN '消息中心'
    WHEN `permission_code` = 'stats:view' AND `menu_type` = 'MENU' THEN '统计分析'
    WHEN `permission_code` = 'system:view' AND `menu_type` = 'MENU' THEN '系统管理'
    WHEN `permission_code` = 'user:view' AND `menu_type` = 'MENU' THEN '用户管理'
    WHEN `permission_code` = 'plan:publish' AND `menu_type` = 'BUTTON' THEN '发布计划'
    WHEN `permission_code` = 'application:assign' AND `menu_type` = 'BUTTON' THEN '执行分配'
    WHEN `permission_code` = 'material:upload' AND `menu_type` = 'BUTTON' THEN '上传材料'
    WHEN `permission_code` = 'material:manage' AND `menu_type` = 'BUTTON' THEN '材料管理'
    WHEN `permission_code` = 'evaluation:submit' AND `menu_type` = 'BUTTON' THEN '提交评价'
    WHEN `permission_code` = 'score:publish' AND `menu_type` = 'BUTTON' THEN '发布成绩'
    WHEN `permission_code` = 'score:adjust' AND `menu_type` = 'BUTTON' THEN '调整成绩'
    WHEN `permission_code` = 'notice:send' AND `menu_type` = 'BUTTON' THEN '发送通知'
    WHEN `permission_code` = 'stats:export' AND `menu_type` = 'BUTTON' THEN '导出报表'
    WHEN `permission_code` = 'user:manage' AND `menu_type` = 'BUTTON' THEN '用户管理'
    WHEN `permission_code` = 'role:manage' AND `menu_type` = 'BUTTON' THEN '角色管理'
    WHEN `permission_code` = 'param:manage' AND `menu_type` = 'BUTTON' THEN '参数管理'
    WHEN `permission_code` = 'auth:profile' AND `menu_type` = 'BUTTON' THEN '个人信息'
    ELSE `menu_name`
END
WHERE `deleted` = 0;

UPDATE `sys_menu`
SET `menu_name` = '用户管理'
WHERE `id` = 2010
  AND `deleted` = 0;
