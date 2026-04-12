-- =============================================
-- 师范生教育实习全过程管理平台
-- 初始化种子数据
-- =============================================

USE `teacher_internship_platform`;

-- 基础字典：院系/专业/年级/基地
INSERT INTO `base_department` (`id`, `dept_code`, `dept_name`, `parent_id`, `leader_name`, `contact_phone`, `status`, `created_by`, `updated_by`)
VALUES
(3001, 'EDU', '教育学院', 0, '张老师', '0773-1111111', 'ENABLED', 0, 0),
(3002, 'CHN', '文学院', 0, '李老师', '0773-2222222', 'ENABLED', 0, 0);

INSERT INTO `base_major` (`id`, `dept_id`, `major_code`, `major_name`, `status`, `created_by`, `updated_by`)
VALUES
(3101, 3001, 'JY001', '教育学', 'ENABLED', 0, 0),
(3102, 3002, 'HW001', '汉语言文学', 'ENABLED', 0, 0);

INSERT INTO `base_grade` (`id`, `grade_code`, `grade_name`, `status`, `created_by`, `updated_by`)
VALUES
(3201, 'G2023', '2023级', 'ENABLED', 0, 0),
(3202, 'G2024', '2024级', 'ENABLED', 0, 0);

INSERT INTO `base_internship_base` (`id`, `base_code`, `base_name`, `province`, `city`, `district`, `address`, `contact_person`, `contact_phone`, `status`, `created_by`, `updated_by`)
VALUES
(3301, 'BASE001', '桂林市第一中学', '广西壮族自治区', '桂林市', '秀峰区', '中山中路1号', '王主任', '0773-3333333', 'ENABLED', 0, 0),
(3302, 'BASE002', '桂林市第二中学', '广西壮族自治区', '桂林市', '七星区', '漓江路2号', '刘主任', '0773-4444444', 'ENABLED', 0, 0);

-- 角色
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `data_scope`, `status`, `remark`, `created_by`, `updated_by`)
VALUES
(1001, 'STUDENT', '师范生', 'SELF', 'ENABLED', '学生角色', 0, 0),
(1002, 'INNER_TEACHER', '校内指导教师', 'SELF', 'ENABLED', '校内导师角色', 0, 0),
(1003, 'BASE_TEACHER', '实习基地指导教师', 'BASE', 'ENABLED', '基地导师角色', 0, 0),
(1004, 'DEPT_ADMIN', '院系管理员', 'DEPT', 'ENABLED', '院系管理角色', 0, 0),
(1005, 'ACADEMIC_ADMIN', '教务处管理员', 'SCHOOL', 'ENABLED', '教务处角色', 0, 0),
(1006, 'SYS_ADMIN', '系统管理员', 'SYSTEM', 'ENABLED', '系统管理角色', 0, 0);

-- 菜单与权限码（菜单+按钮）
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `route_path`, `component_path`, `permission_code`, `icon`, `sort_no`, `visible`, `status`, `remark`, `created_by`, `updated_by`)
VALUES
(2001, 0, '工作台', 'MENU', '/dashboard', 'views/dashboard/DashboardView', 'dashboard:view', 'Odometer', 1, 1, 'ENABLED', '工作台', 0, 0),
(2002, 0, '实习计划', 'MENU', '/plan', 'views/plan/PlanListView', 'plan:view', 'Calendar', 2, 1, 'ENABLED', '实习计划', 0, 0),
(2003, 0, '申请分配', 'MENU', '/application', 'views/application/ApplicationListView', 'application:view', 'Switch', 3, 1, 'ENABLED', '申请与分配', 0, 0),
(2004, 0, '过程材料', 'MENU', '/material', 'views/material/MaterialListView', 'material:view', 'FolderOpened', 4, 1, 'ENABLED', '材料管理', 0, 0),
(2005, 0, '指导评价', 'MENU', '/evaluation', 'views/evaluation/EvaluationListView', 'evaluation:view', 'EditPen', 5, 1, 'ENABLED', '评价管理', 0, 0),
(2006, 0, '成绩管理', 'MENU', '/score', 'views/score/ScoreListView', 'score:view', 'DataLine', 6, 1, 'ENABLED', '成绩管理', 0, 0),
(2007, 0, '消息中心', 'MENU', '/notice', 'views/notice/NoticeListView', 'notice:view', 'Bell', 7, 1, 'ENABLED', '消息中心', 0, 0),
(2008, 0, '统计分析', 'MENU', '/stats', 'views/stats/StatsView', 'stats:view', 'PieChart', 8, 1, 'ENABLED', '统计分析', 0, 0),
(2009, 0, '系统管理', 'MENU', '/system', 'views/system/SystemView', 'system:view', 'Setting', 9, 1, 'ENABLED', '系统管理', 0, 0),
(2010, 2009, '用户管理', 'MENU', '/system/users', 'views/system/UserManagementView', 'user:view', 'UserFilled', 1, 1, 'ENABLED', '用户管理菜单', 0, 0),
(2011, 2009, '基础数据维护', 'MENU', '/system/base', 'views/system/SystemBaseDataView', NULL, 'OfficeBuilding', 2, 1, 'ENABLED', '基础数据维护菜单', 0, 0),
(2012, 2009, '系统参数', 'MENU', '/system/params', 'views/system/SystemParamView', NULL, 'Tools', 3, 1, 'ENABLED', '系统参数菜单', 0, 0),
(2013, 2009, '日志审计', 'MENU', '/system/logs', 'views/system/SystemLogView', NULL, 'Document', 4, 1, 'ENABLED', '日志审计菜单', 0, 0),
(2014, 2009, '角色权限配置', 'MENU', '/system/rbac', 'views/system/SystemRbacView', NULL, 'Lock', 5, 1, 'ENABLED', '角色权限配置菜单', 0, 0),
(2101, 2002, '发布计划', 'BUTTON', NULL, NULL, 'plan:publish', NULL, 1, 1, 'ENABLED', '发布计划按钮', 0, 0),
(2102, 2003, '执行分配', 'BUTTON', NULL, NULL, 'application:assign', NULL, 1, 1, 'ENABLED', '执行分配按钮', 0, 0),
(2103, 2004, '上传材料', 'BUTTON', NULL, NULL, 'material:upload', NULL, 1, 1, 'ENABLED', '上传材料按钮', 0, 0),
(2104, 2005, '提交评价', 'BUTTON', NULL, NULL, 'evaluation:submit', NULL, 1, 1, 'ENABLED', '提交评价按钮', 0, 0),
(2105, 2006, '发布成绩', 'BUTTON', NULL, NULL, 'score:publish', NULL, 1, 1, 'ENABLED', '发布成绩按钮', 0, 0),
(2106, 2006, '调整成绩', 'BUTTON', NULL, NULL, 'score:adjust', NULL, 2, 1, 'ENABLED', '调整成绩按钮', 0, 0),
(2107, 2007, '发送通知', 'BUTTON', NULL, NULL, 'notice:send', NULL, 1, 1, 'ENABLED', '发送通知按钮', 0, 0),
(2108, 2008, '导出报表', 'BUTTON', NULL, NULL, 'stats:export', NULL, 1, 1, 'ENABLED', '导出统计按钮', 0, 0),
(2109, 2009, '用户管理', 'BUTTON', NULL, NULL, 'user:manage', NULL, 1, 1, 'ENABLED', '用户管理按钮', 0, 0),
(2110, 2009, '角色管理', 'BUTTON', NULL, NULL, 'role:manage', NULL, 2, 1, 'ENABLED', '角色管理按钮', 0, 0),
(2111, 2009, '参数管理', 'BUTTON', NULL, NULL, 'param:manage', NULL, 3, 1, 'ENABLED', '参数管理按钮', 0, 0),
(2112, 2001, '个人信息', 'BUTTON', NULL, NULL, 'auth:profile', NULL, 1, 1, 'ENABLED', '个人信息按钮', 0, 0),
(2113, 2004, '材料管理', 'BUTTON', NULL, NULL, 'material:manage', NULL, 2, 1, 'ENABLED', '材料管理按钮', 0, 0);

-- 角色菜单关系
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_by`, `updated_by`)
VALUES
(300001, 1001, 2001, 0, 0), (300002, 1001, 2002, 0, 0), (300003, 1001, 2003, 0, 0),
(300004, 1001, 2004, 0, 0), (300005, 1001, 2005, 0, 0), (300006, 1001, 2006, 0, 0),
(300007, 1001, 2007, 0, 0), (300008, 1001, 2103, 0, 0), (300009, 1001, 2112, 0, 0),

(300010, 1002, 2001, 0, 0), (300011, 1002, 2004, 0, 0), (300012, 1002, 2005, 0, 0),
(300013, 1002, 2006, 0, 0), (300014, 1002, 2007, 0, 0), (300015, 1002, 2104, 0, 0),
(300016, 1002, 2112, 0, 0),

(300017, 1003, 2001, 0, 0), (300018, 1003, 2004, 0, 0), (300019, 1003, 2005, 0, 0),
(300020, 1003, 2006, 0, 0), (300021, 1003, 2007, 0, 0), (300022, 1003, 2104, 0, 0),
(300023, 1003, 2112, 0, 0),

(300024, 1004, 2001, 0, 0), (300025, 1004, 2002, 0, 0), (300026, 1004, 2003, 0, 0),
(300027, 1004, 2004, 0, 0), (300029, 1004, 2006, 0, 0), (300030, 1004, 2007, 0, 0),
(300031, 1004, 2008, 0, 0), (300032, 1004, 2101, 0, 0), (300033, 1004, 2102, 0, 0),
(300034, 1004, 2105, 0, 0), (300035, 1004, 2106, 0, 0), (300036, 1004, 2108, 0, 0),
(300037, 1004, 2112, 0, 0),

(300038, 1005, 2001, 0, 0), (300039, 1005, 2006, 0, 0), (300040, 1005, 2007, 0, 0),
(300041, 1005, 2008, 0, 0), (300042, 1005, 2108, 0, 0), (300043, 1005, 2112, 0, 0),

(300044, 1006, 2001, 0, 0), (300045, 1006, 2002, 0, 0), (300046, 1006, 2003, 0, 0),
(300047, 1006, 2004, 0, 0), (300048, 1006, 2005, 0, 0), (300049, 1006, 2006, 0, 0),
(300050, 1006, 2007, 0, 0), (300051, 1006, 2008, 0, 0), (300052, 1006, 2009, 0, 0),
(300065, 1006, 2010, 0, 0),
(300080, 1006, 2011, 0, 0), (300081, 1006, 2012, 0, 0), (300082, 1006, 2013, 0, 0),
(300083, 1006, 2014, 0, 0),
(300053, 1006, 2101, 0, 0), (300054, 1006, 2102, 0, 0), (300055, 1006, 2103, 0, 0),
(300056, 1006, 2104, 0, 0), (300057, 1006, 2105, 0, 0), (300058, 1006, 2106, 0, 0),
(300059, 1006, 2107, 0, 0), (300060, 1006, 2108, 0, 0), (300061, 1006, 2109, 0, 0),
(300062, 1006, 2110, 0, 0), (300063, 1006, 2111, 0, 0), (300064, 1006, 2112, 0, 0);

-- 测试用户（默认密码：123456）
-- BCrypt: $2b$12$stvX6qCFqFlDatqMQeg1aOMyjf8EdeYvIKBcquWFIrMJGYySPfmvG
INSERT INTO `sys_user`
(`id`, `login_name`, `password_hash`, `real_name`, `identity_type`, `student_no`, `teacher_no`, `phone`, `email`, `dept_id`, `major_id`, `grade_id`, `status`, `must_change_password`, `created_by`, `updated_by`)
VALUES
(5001, 'A0001', '$2b$12$stvX6qCFqFlDatqMQeg1aOMyjf8EdeYvIKBcquWFIrMJGYySPfmvG', '系统管理员', 'ADMIN', NULL, 'A0001', '13800000001', 'admin001@example.com', 3001, NULL, NULL, 'ENABLED', 0, 0, 0),
(5002, 'D0001', '$2b$12$stvX6qCFqFlDatqMQeg1aOMyjf8EdeYvIKBcquWFIrMJGYySPfmvG', '教育学院管理员', 'ADMIN', NULL, 'D0001', '13800000002', 'dept001@example.com', 3001, NULL, NULL, 'ENABLED', 0, 0, 0),
(5003, 'J0001', '$2b$12$stvX6qCFqFlDatqMQeg1aOMyjf8EdeYvIKBcquWFIrMJGYySPfmvG', '教务处管理员', 'ADMIN', NULL, 'J0001', '13800000003', 'jw001@example.com', 3001, NULL, NULL, 'ENABLED', 0, 0, 0),
(5004, 'T0001', '$2b$12$stvX6qCFqFlDatqMQeg1aOMyjf8EdeYvIKBcquWFIrMJGYySPfmvG', '校内指导教师', 'TEACHER', NULL, 'T0001', '13800000004', 't001@example.com', 3001, 3101, NULL, 'ENABLED', 0, 0, 0),
(5005, 'B0001', '$2b$12$stvX6qCFqFlDatqMQeg1aOMyjf8EdeYvIKBcquWFIrMJGYySPfmvG', '基地指导教师', 'TEACHER', NULL, 'B0001', '13800000005', 'bt001@example.com', 3001, 3101, NULL, 'ENABLED', 0, 0, 0),
(5006, '20230001', '$2b$12$stvX6qCFqFlDatqMQeg1aOMyjf8EdeYvIKBcquWFIrMJGYySPfmvG', '师范生甲', 'STUDENT', '20230001', NULL, '13800000006', 'stu001@example.com', 3001, 3101, 3201, 'ENABLED', 0, 0, 0),
(5007, '20230002', '$2b$12$stvX6qCFqFlDatqMQeg1aOMyjf8EdeYvIKBcquWFIrMJGYySPfmvG', '师范生乙', 'STUDENT', '20230002', NULL, '13800000007', 'stu002@example.com', 3001, 3101, 3201, 'ENABLED', 0, 0, 0);

-- 用户角色关系
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `created_by`, `updated_by`)
VALUES
(400001, 5001, 1006, 0, 0),
(400002, 5002, 1004, 0, 0),
(400003, 5003, 1005, 0, 0),
(400004, 5004, 1002, 0, 0),
(400005, 5005, 1003, 0, 0),
(400006, 5006, 1001, 0, 0),
(400007, 5007, 1001, 0, 0);

-- 系统参数
INSERT INTO `sys_param` (`id`, `param_code`, `param_name`, `param_value`, `param_type`, `status`, `remark`, `created_by`, `updated_by`)
VALUES
(6001, 'SYSTEM_FILE_MAX_SIZE_MB', '上传文件大小上限(MB)', '20', 'SYSTEM', 'ENABLED', '对应业务上传限制', 0, 0),
(6002, 'SYSTEM_LOGIN_REMEMBER_DAYS', '记住我有效天数', '7', 'SYSTEM', 'ENABLED', '登录记住我有效期', 0, 0),
(6003, 'SCORE_DECIMAL_SCALE', '成绩保留小数位', '2', 'BUSINESS', 'ENABLED', '成绩计算保留精度', 0, 0),
(6004, 'NOTICE_NEAR_DEADLINE_DAYS', '材料临期提醒天数', '3', 'BUSINESS', 'ENABLED', '材料截止前提醒天数', 0, 0),
(6005, 'PLAN_DEFAULT_QUOTA', '默认计划名额', '100', 'BUSINESS', 'ENABLED', '创建计划默认名额', 0, 0);

