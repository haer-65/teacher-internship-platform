-- =============================================
-- 师范生教育实习全过程管理平台
-- MySQL 8.0 初始化建库建表脚本
-- =============================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE DATABASE IF NOT EXISTS `teacher_internship_platform`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE `teacher_internship_platform`;

-- =========================
-- 基础数据表
-- =========================

DROP TABLE IF EXISTS `base_internship_base`;
DROP TABLE IF EXISTS `base_grade`;
DROP TABLE IF EXISTS `base_major`;
DROP TABLE IF EXISTS `base_department`;

CREATE TABLE `base_department` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `dept_code` VARCHAR(32) NOT NULL COMMENT '院系编码',
  `dept_name` VARCHAR(64) NOT NULL COMMENT '院系名称',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父级院系ID，0表示根节点',
  `leader_name` VARCHAR(64) DEFAULT NULL COMMENT '负责人姓名',
  `contact_phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_base_department_code` (`dept_code`, `deleted`),
  KEY `idx_base_department_parent_id` (`parent_id`),
  KEY `idx_base_department_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='院系基础数据表';

CREATE TABLE `base_major` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `dept_id` BIGINT UNSIGNED NOT NULL COMMENT '所属院系ID',
  `major_code` VARCHAR(32) NOT NULL COMMENT '专业编码',
  `major_name` VARCHAR(64) NOT NULL COMMENT '专业名称',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_base_major_code` (`major_code`, `deleted`),
  KEY `idx_base_major_dept_id` (`dept_id`),
  KEY `idx_base_major_status` (`status`),
  CONSTRAINT `fk_base_major_dept` FOREIGN KEY (`dept_id`) REFERENCES `base_department` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='专业基础数据表';

CREATE TABLE `base_grade` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `grade_code` VARCHAR(32) NOT NULL COMMENT '年级编码',
  `grade_name` VARCHAR(32) NOT NULL COMMENT '年级名称',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_base_grade_code` (`grade_code`, `deleted`),
  KEY `idx_base_grade_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='年级基础数据表';

CREATE TABLE `base_internship_base` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `base_code` VARCHAR(32) NOT NULL COMMENT '实习基地编码',
  `base_name` VARCHAR(128) NOT NULL COMMENT '实习基地名称',
  `province` VARCHAR(64) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(64) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(64) DEFAULT NULL COMMENT '区县',
  `address` VARCHAR(255) DEFAULT NULL COMMENT '详细地址',
  `contact_person` VARCHAR(64) DEFAULT NULL COMMENT '联系人',
  `contact_phone` VARCHAR(32) DEFAULT NULL COMMENT '联系电话',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_base_internship_base_code` (`base_code`, `deleted`),
  KEY `idx_base_internship_base_name` (`base_name`),
  KEY `idx_base_internship_base_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实习基地基础数据表';

-- =========================
-- 系统表
-- =========================

DROP TABLE IF EXISTS `sys_param`;
DROP TABLE IF EXISTS `sys_operation_log`;
DROP TABLE IF EXISTS `sys_login_log`;
DROP TABLE IF EXISTS `sys_role_menu`;
DROP TABLE IF EXISTS `sys_user_role`;
DROP TABLE IF EXISTS `sys_menu`;
DROP TABLE IF EXISTS `sys_user`;
DROP TABLE IF EXISTS `sys_role`;

CREATE TABLE `sys_role` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `role_code` VARCHAR(32) NOT NULL COMMENT '角色编码',
  `role_name` VARCHAR(64) NOT NULL COMMENT '角色名称',
  `data_scope` VARCHAR(16) NOT NULL COMMENT '数据范围：SELF/DEPT/SCHOOL/BASE/SYSTEM',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_code` (`role_code`, `deleted`),
  KEY `idx_sys_role_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

CREATE TABLE `sys_menu` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `parent_id` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '父菜单ID',
  `menu_name` VARCHAR(64) NOT NULL COMMENT '菜单名称',
  `menu_type` VARCHAR(16) NOT NULL COMMENT '类型：MENU/BUTTON',
  `route_path` VARCHAR(255) DEFAULT NULL COMMENT '路由路径',
  `component_path` VARCHAR(255) DEFAULT NULL COMMENT '组件路径',
  `permission_code` VARCHAR(128) DEFAULT NULL COMMENT '权限码',
  `icon` VARCHAR(64) DEFAULT NULL COMMENT '图标',
  `sort_no` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `visible` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否可见：1是0否',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_menu_permission` (`permission_code`, `deleted`),
  KEY `idx_sys_menu_parent_sort` (`parent_id`, `sort_no`),
  KEY `idx_sys_menu_type` (`menu_type`),
  KEY `idx_sys_menu_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='菜单权限表';

CREATE TABLE `sys_user` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `login_name` VARCHAR(64) NOT NULL COMMENT '登录名（学号/工号）',
  `password_hash` VARCHAR(100) NOT NULL COMMENT '密码哈希（BCrypt）',
  `real_name` VARCHAR(64) NOT NULL COMMENT '真实姓名',
  `identity_type` VARCHAR(16) NOT NULL COMMENT '身份类型：STUDENT/TEACHER/ADMIN',
  `student_no` VARCHAR(32) DEFAULT NULL COMMENT '学号',
  `teacher_no` VARCHAR(32) DEFAULT NULL COMMENT '工号',
  `phone` VARCHAR(32) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
  `dept_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '院系ID',
  `major_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '专业ID',
  `grade_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '年级ID',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：PENDING/ENABLED/DISABLED/LOCKED',
  `must_change_password` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否强制改密',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_login_name` (`login_name`, `deleted`),
  UNIQUE KEY `uk_sys_user_student_no` (`student_no`, `deleted`),
  UNIQUE KEY `uk_sys_user_teacher_no` (`teacher_no`, `deleted`),
  KEY `idx_sys_user_identity_type` (`identity_type`),
  KEY `idx_sys_user_dept_id` (`dept_id`),
  KEY `idx_sys_user_major_id` (`major_id`),
  KEY `idx_sys_user_grade_id` (`grade_id`),
  KEY `idx_sys_user_status` (`status`),
  CONSTRAINT `fk_sys_user_dept` FOREIGN KEY (`dept_id`) REFERENCES `base_department` (`id`),
  CONSTRAINT `fk_sys_user_major` FOREIGN KEY (`major_id`) REFERENCES `base_major` (`id`),
  CONSTRAINT `fk_sys_user_grade` FOREIGN KEY (`grade_id`) REFERENCES `base_grade` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

CREATE TABLE `sys_user_role` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_user_role` (`user_id`, `role_id`, `deleted`),
  KEY `idx_sys_user_role_role_id` (`role_id`),
  CONSTRAINT `fk_sys_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_sys_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

CREATE TABLE `sys_role_menu` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `role_id` BIGINT UNSIGNED NOT NULL COMMENT '角色ID',
  `menu_id` BIGINT UNSIGNED NOT NULL COMMENT '菜单ID',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_role_menu` (`role_id`, `menu_id`, `deleted`),
  KEY `idx_sys_role_menu_menu_id` (`menu_id`),
  CONSTRAINT `fk_sys_role_menu_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`),
  CONSTRAINT `fk_sys_role_menu_menu` FOREIGN KEY (`menu_id`) REFERENCES `sys_menu` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色菜单关联表';

CREATE TABLE `sys_login_log` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `login_name` VARCHAR(64) NOT NULL COMMENT '登录名',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '用户ID',
  `login_ip` VARCHAR(64) DEFAULT NULL COMMENT '登录IP',
  `user_agent` VARCHAR(512) DEFAULT NULL COMMENT '客户端UA',
  `login_result` VARCHAR(16) NOT NULL COMMENT '登录结果：SUCCESS/FAIL',
  `fail_reason` VARCHAR(255) DEFAULT NULL COMMENT '失败原因',
  `login_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  KEY `idx_sys_login_log_login_name_time` (`login_name`, `login_time`),
  KEY `idx_sys_login_log_result` (`login_result`),
  CONSTRAINT `fk_sys_login_log_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

CREATE TABLE `sys_operation_log` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `operator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作人ID',
  `operator_name` VARCHAR(64) DEFAULT NULL COMMENT '操作人姓名',
  `module_code` VARCHAR(64) NOT NULL COMMENT '模块编码',
  `action_code` VARCHAR(64) NOT NULL COMMENT '操作编码',
  `business_type` VARCHAR(64) DEFAULT NULL COMMENT '业务类型',
  `business_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '业务ID',
  `request_method` VARCHAR(16) DEFAULT NULL COMMENT '请求方法',
  `request_uri` VARCHAR(255) DEFAULT NULL COMMENT '请求地址',
  `request_ip` VARCHAR(64) DEFAULT NULL COMMENT '请求IP',
  `request_params` TEXT COMMENT '请求参数',
  `response_data` TEXT COMMENT '响应数据',
  `operation_status` VARCHAR(16) NOT NULL COMMENT '结果：SUCCESS/FAIL',
  `error_message` VARCHAR(1000) DEFAULT NULL COMMENT '异常信息',
  `operate_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  KEY `idx_sys_operation_log_operator_time` (`operator_id`, `operate_time`),
  KEY `idx_sys_operation_log_module_action` (`module_code`, `action_code`),
  KEY `idx_sys_operation_log_status` (`operation_status`),
  CONSTRAINT `fk_sys_operation_log_operator` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

CREATE TABLE `sys_param` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `param_code` VARCHAR(64) NOT NULL COMMENT '参数编码',
  `param_name` VARCHAR(128) NOT NULL COMMENT '参数名称',
  `param_value` VARCHAR(1000) NOT NULL COMMENT '参数值',
  `param_type` VARCHAR(32) NOT NULL DEFAULT 'SYSTEM' COMMENT '参数类型：SYSTEM/BUSINESS',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT '备注',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_param_code` (`param_code`, `deleted`),
  KEY `idx_sys_param_type` (`param_type`),
  KEY `idx_sys_param_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统参数表';

-- =========================
-- 实习业务表
-- =========================

DROP TABLE IF EXISTS `biz_notice_receiver`;
DROP TABLE IF EXISTS `biz_notice`;
DROP TABLE IF EXISTS `biz_score_sheet`;
DROP TABLE IF EXISTS `biz_evaluation`;
DROP TABLE IF EXISTS `biz_material_version`;
DROP TABLE IF EXISTS `biz_material`;
DROP TABLE IF EXISTS `biz_assignment`;
DROP TABLE IF EXISTS `biz_student_application`;
DROP TABLE IF EXISTS `biz_material_type`;
DROP TABLE IF EXISTS `biz_plan_attachment`;
DROP TABLE IF EXISTS `biz_internship_plan`;

CREATE TABLE `biz_internship_plan` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `plan_code` VARCHAR(32) NOT NULL COMMENT '计划编码',
  `plan_name` VARCHAR(128) NOT NULL COMMENT '计划名称',
  `academic_year` VARCHAR(16) NOT NULL COMMENT '学年',
  `term` VARCHAR(16) NOT NULL COMMENT '学期',
  `dept_id` BIGINT UNSIGNED NOT NULL COMMENT '所属院系ID',
  `start_time` DATETIME NOT NULL COMMENT '实习开始时间',
  `end_time` DATETIME NOT NULL COMMENT '实习结束时间',
  `apply_deadline` DATETIME NOT NULL COMMENT '申请截止时间',
  `student_quota` INT NOT NULL DEFAULT 0 COMMENT '计划名额',
  `description` VARCHAR(1000) DEFAULT NULL COMMENT '计划说明',
  `inner_teacher_weight` DECIMAL(5,2) NOT NULL DEFAULT 50.00 COMMENT '校内导师权重',
  `base_teacher_weight` DECIMAL(5,2) NOT NULL DEFAULT 50.00 COMMENT '基地导师权重',
  `plan_status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED/FINISHED/ARCHIVED',
  `score_publish_status` VARCHAR(16) NOT NULL DEFAULT 'UNPUBLISHED' COMMENT '成绩发布状态：UNPUBLISHED/PUBLISHED',
  `published_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `archived_time` DATETIME DEFAULT NULL COMMENT '归档时间',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_plan_code` (`plan_code`, `deleted`),
  KEY `idx_biz_plan_dept_status` (`dept_id`, `plan_status`),
  KEY `idx_biz_plan_apply_deadline` (`apply_deadline`),
  KEY `idx_biz_plan_time_range` (`start_time`, `end_time`),
  CONSTRAINT `fk_biz_plan_dept` FOREIGN KEY (`dept_id`) REFERENCES `base_department` (`id`),
  CONSTRAINT `ck_biz_plan_weight_sum` CHECK (`inner_teacher_weight` + `base_teacher_weight` = 100.00)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实习计划表';

CREATE TABLE `biz_plan_attachment` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `plan_id` BIGINT UNSIGNED NOT NULL COMMENT '计划ID',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件相对路径',
  `file_size` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件大小（字节）',
  `file_ext` VARCHAR(32) DEFAULT NULL COMMENT '文件扩展名',
  `mime_type` VARCHAR(128) DEFAULT NULL COMMENT 'MIME类型',
  `uploaded_by` BIGINT UNSIGNED NOT NULL COMMENT '上传人ID',
  `uploaded_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  KEY `idx_biz_plan_attachment_plan_id` (`plan_id`),
  KEY `idx_biz_plan_attachment_uploaded_by` (`uploaded_by`),
  CONSTRAINT `fk_biz_plan_attachment_plan` FOREIGN KEY (`plan_id`) REFERENCES `biz_internship_plan` (`id`),
  CONSTRAINT `fk_biz_plan_attachment_uploaded_by` FOREIGN KEY (`uploaded_by`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划附件表';

CREATE TABLE `biz_material_type` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `plan_id` BIGINT UNSIGNED NOT NULL COMMENT '计划ID',
  `type_code` VARCHAR(32) NOT NULL COMMENT '材料类型编码',
  `type_name` VARCHAR(64) NOT NULL COMMENT '材料类型名称',
  `required_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否必交',
  `max_submit_count` INT NOT NULL DEFAULT 10 COMMENT '最大提交次数',
  `deadline_time` DATETIME NOT NULL COMMENT '提交截止时间',
  `weight` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '权重',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT '状态：ENABLED/DISABLED',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_material_type_plan_code` (`plan_id`, `type_code`, `deleted`),
  KEY `idx_biz_material_type_plan_deadline` (`plan_id`, `deadline_time`),
  CONSTRAINT `fk_biz_material_type_plan` FOREIGN KEY (`plan_id`) REFERENCES `biz_internship_plan` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='计划材料类型配置表';

CREATE TABLE `biz_student_application` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `plan_id` BIGINT UNSIGNED NOT NULL COMMENT '计划ID',
  `student_id` BIGINT UNSIGNED NOT NULL COMMENT '学生用户ID',
  `intention_region` VARCHAR(128) DEFAULT NULL COMMENT '意向地区',
  `school_type` VARCHAR(64) DEFAULT NULL COMMENT '学校类型',
  `personal_statement` VARCHAR(1000) DEFAULT NULL COMMENT '个人说明',
  `application_status` VARCHAR(16) NOT NULL DEFAULT 'SUBMITTED' COMMENT '状态：SUBMITTED/APPROVED/REJECTED/WITHDRAWN',
  `review_comment` VARCHAR(500) DEFAULT NULL COMMENT '审核意见',
  `reviewed_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '审核人ID',
  `reviewed_time` DATETIME DEFAULT NULL COMMENT '审核时间',
  `submitted_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_application_plan_student` (`plan_id`, `student_id`, `deleted`),
  KEY `idx_biz_application_student_status` (`student_id`, `application_status`),
  KEY `idx_biz_application_plan_status` (`plan_id`, `application_status`),
  KEY `idx_biz_application_reviewed_by` (`reviewed_by`),
  CONSTRAINT `fk_biz_application_plan` FOREIGN KEY (`plan_id`) REFERENCES `biz_internship_plan` (`id`),
  CONSTRAINT `fk_biz_application_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_biz_application_reviewed_by` FOREIGN KEY (`reviewed_by`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='学生实习申请表';

CREATE TABLE `biz_assignment` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `plan_id` BIGINT UNSIGNED NOT NULL COMMENT '计划ID',
  `application_id` BIGINT UNSIGNED NOT NULL COMMENT '申请ID',
  `student_id` BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
  `base_id` BIGINT UNSIGNED NOT NULL COMMENT '实习基地ID',
  `inner_teacher_id` BIGINT UNSIGNED NOT NULL COMMENT '校内导师ID',
  `base_teacher_id` BIGINT UNSIGNED NOT NULL COMMENT '基地导师ID',
  `version_no` INT NOT NULL DEFAULT 1 COMMENT '分配版本号',
  `is_current` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否当前版本',
  `assignment_status` VARCHAR(16) NOT NULL DEFAULT 'ASSIGNED' COMMENT '状态：ASSIGNED/ADJUSTED/CANCELLED',
  `adjust_reason` VARCHAR(500) DEFAULT NULL COMMENT '调整原因',
  `assigned_by` BIGINT UNSIGNED NOT NULL COMMENT '分配人ID',
  `assigned_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '分配时间',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_assignment_plan_student_version` (`plan_id`, `student_id`, `version_no`, `deleted`),
  KEY `idx_biz_assignment_current` (`plan_id`, `student_id`, `is_current`),
  KEY `idx_biz_assignment_base_id` (`base_id`),
  KEY `idx_biz_assignment_inner_teacher_id` (`inner_teacher_id`),
  KEY `idx_biz_assignment_base_teacher_id` (`base_teacher_id`),
  CONSTRAINT `fk_biz_assignment_plan` FOREIGN KEY (`plan_id`) REFERENCES `biz_internship_plan` (`id`),
  CONSTRAINT `fk_biz_assignment_application` FOREIGN KEY (`application_id`) REFERENCES `biz_student_application` (`id`),
  CONSTRAINT `fk_biz_assignment_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_biz_assignment_base` FOREIGN KEY (`base_id`) REFERENCES `base_internship_base` (`id`),
  CONSTRAINT `fk_biz_assignment_inner_teacher` FOREIGN KEY (`inner_teacher_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_biz_assignment_base_teacher` FOREIGN KEY (`base_teacher_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_biz_assignment_assigned_by` FOREIGN KEY (`assigned_by`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='实习分配表';

CREATE TABLE `biz_material` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `assignment_id` BIGINT UNSIGNED NOT NULL COMMENT '分配ID',
  `material_type_id` BIGINT UNSIGNED NOT NULL COMMENT '材料类型ID',
  `student_id` BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
  `latest_version_no` INT NOT NULL DEFAULT 0 COMMENT '最新版本号',
  `material_status` VARCHAR(16) NOT NULL DEFAULT 'NOT_SUBMITTED' COMMENT '状态：NOT_SUBMITTED/SUBMITTED/OVERDUE',
  `last_submit_time` DATETIME DEFAULT NULL COMMENT '最后提交时间',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_material_assignment_type` (`assignment_id`, `material_type_id`, `deleted`),
  KEY `idx_biz_material_student_id` (`student_id`),
  KEY `idx_biz_material_status` (`material_status`),
  CONSTRAINT `fk_biz_material_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `biz_assignment` (`id`),
  CONSTRAINT `fk_biz_material_type` FOREIGN KEY (`material_type_id`) REFERENCES `biz_material_type` (`id`),
  CONSTRAINT `fk_biz_material_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='材料主表（按分配+类型聚合）';

CREATE TABLE `biz_material_version` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `material_id` BIGINT UNSIGNED NOT NULL COMMENT '材料主表ID',
  `version_no` INT NOT NULL COMMENT '版本号',
  `file_name` VARCHAR(255) NOT NULL COMMENT '文件名',
  `file_path` VARCHAR(500) NOT NULL COMMENT '文件相对路径',
  `file_size` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '文件大小',
  `file_ext` VARCHAR(32) DEFAULT NULL COMMENT '扩展名',
  `mime_type` VARCHAR(128) DEFAULT NULL COMMENT 'MIME类型',
  `submit_remark` VARCHAR(500) DEFAULT NULL COMMENT '提交备注',
  `submitted_by` BIGINT UNSIGNED NOT NULL COMMENT '提交人ID',
  `submitted_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `is_current` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否当前版本',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_material_version` (`material_id`, `version_no`, `deleted`),
  KEY `idx_biz_material_version_current` (`material_id`, `is_current`),
  KEY `idx_biz_material_version_submitted_by` (`submitted_by`),
  CONSTRAINT `fk_biz_material_version_material` FOREIGN KEY (`material_id`) REFERENCES `biz_material` (`id`),
  CONSTRAINT `fk_biz_material_version_submitted_by` FOREIGN KEY (`submitted_by`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='材料版本表';

CREATE TABLE `biz_evaluation` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `plan_id` BIGINT UNSIGNED NOT NULL COMMENT '计划ID',
  `assignment_id` BIGINT UNSIGNED NOT NULL COMMENT '分配ID',
  `student_id` BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
  `material_id` BIGINT UNSIGNED NOT NULL COMMENT '材料主表ID',
  `material_version_id` BIGINT UNSIGNED NOT NULL COMMENT '材料版本ID',
  `evaluator_id` BIGINT UNSIGNED NOT NULL COMMENT '评价人ID',
  `evaluator_role` VARCHAR(32) NOT NULL COMMENT '评价人角色',
  `evaluation_type` VARCHAR(16) NOT NULL COMMENT '评价类型：PROCESS/FINAL',
  `score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '评分',
  `comment_text` VARCHAR(1000) DEFAULT NULL COMMENT '评语',
  `evaluated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_evaluation_version_evaluator_type` (`material_version_id`, `evaluator_id`, `evaluation_type`, `deleted`),
  KEY `idx_biz_evaluation_plan_student` (`plan_id`, `student_id`),
  KEY `idx_biz_evaluation_assignment` (`assignment_id`),
  CONSTRAINT `fk_biz_evaluation_plan` FOREIGN KEY (`plan_id`) REFERENCES `biz_internship_plan` (`id`),
  CONSTRAINT `fk_biz_evaluation_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `biz_assignment` (`id`),
  CONSTRAINT `fk_biz_evaluation_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_biz_evaluation_material` FOREIGN KEY (`material_id`) REFERENCES `biz_material` (`id`),
  CONSTRAINT `fk_biz_evaluation_material_version` FOREIGN KEY (`material_version_id`) REFERENCES `biz_material_version` (`id`),
  CONSTRAINT `fk_biz_evaluation_evaluator` FOREIGN KEY (`evaluator_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价记录表（绑定材料版本）';

CREATE TABLE `biz_score_sheet` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `plan_id` BIGINT UNSIGNED NOT NULL COMMENT '计划ID',
  `assignment_id` BIGINT UNSIGNED NOT NULL COMMENT '分配ID',
  `student_id` BIGINT UNSIGNED NOT NULL COMMENT '学生ID',
  `process_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '过程得分',
  `final_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '综合评价得分',
  `total_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT '总分',
  `status` VARCHAR(16) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT/PUBLISHED',
  `adjusted_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否人工调整',
  `adjust_reason` VARCHAR(500) DEFAULT NULL COMMENT '调整原因',
  `adjust_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '调整人ID',
  `adjust_time` DATETIME DEFAULT NULL COMMENT '调整时间',
  `adjust_times` INT NOT NULL DEFAULT 0 COMMENT '调整次数',
  `published_by` BIGINT UNSIGNED DEFAULT NULL COMMENT '发布人ID',
  `published_time` DATETIME DEFAULT NULL COMMENT '发布时间',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_score_plan_student` (`plan_id`, `student_id`, `deleted`),
  KEY `idx_biz_score_status` (`status`),
  KEY `idx_biz_score_total_score` (`total_score`),
  CONSTRAINT `fk_biz_score_plan` FOREIGN KEY (`plan_id`) REFERENCES `biz_internship_plan` (`id`),
  CONSTRAINT `fk_biz_score_assignment` FOREIGN KEY (`assignment_id`) REFERENCES `biz_assignment` (`id`),
  CONSTRAINT `fk_biz_score_student` FOREIGN KEY (`student_id`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_biz_score_adjust_by` FOREIGN KEY (`adjust_by`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_biz_score_published_by` FOREIGN KEY (`published_by`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='成绩单表';

CREATE TABLE `biz_notice` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `notice_type` VARCHAR(32) NOT NULL COMMENT '通知类型：SYSTEM/BUSINESS',
  `notice_level` VARCHAR(16) NOT NULL DEFAULT 'NORMAL' COMMENT '通知级别：NORMAL/URGENT',
  `notice_title` VARCHAR(128) NOT NULL COMMENT '通知标题',
  `notice_content` TEXT NOT NULL COMMENT '通知内容',
  `sender_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '发送人ID',
  `target_role_code` VARCHAR(32) DEFAULT NULL COMMENT '目标角色编码（可空）',
  `related_business_type` VARCHAR(64) DEFAULT NULL COMMENT '关联业务类型',
  `related_business_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '关联业务ID',
  `send_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `status` VARCHAR(16) NOT NULL DEFAULT 'SENT' COMMENT '状态：DRAFT/SENT/RECALLED',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  KEY `idx_biz_notice_type_status` (`notice_type`, `status`),
  KEY `idx_biz_notice_send_time` (`send_time`),
  KEY `idx_biz_notice_sender_id` (`sender_id`),
  CONSTRAINT `fk_biz_notice_sender` FOREIGN KEY (`sender_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知主表';

CREATE TABLE `biz_notice_receiver` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT '主键ID',
  `notice_id` BIGINT UNSIGNED NOT NULL COMMENT '通知ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收人ID',
  `read_flag` TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已读',
  `read_time` DATETIME DEFAULT NULL COMMENT '已读时间',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '创建人',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '更新人',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT '逻辑删除标记：0未删除，>0删除时间戳',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_notice_receiver_notice_user` (`notice_id`, `receiver_id`, `deleted`),
  KEY `idx_biz_notice_receiver_read` (`receiver_id`, `read_flag`),
  CONSTRAINT `fk_biz_notice_receiver_notice` FOREIGN KEY (`notice_id`) REFERENCES `biz_notice` (`id`),
  CONSTRAINT `fk_biz_notice_receiver_user` FOREIGN KEY (`receiver_id`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知接收关系表';

SET FOREIGN_KEY_CHECKS = 1;
