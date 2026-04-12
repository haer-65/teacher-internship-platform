USE `teacher_internship_platform`;

CREATE TABLE IF NOT EXISTS `biz_plan_base` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT 'Primary key',
  `plan_id` BIGINT UNSIGNED NOT NULL COMMENT 'Plan ID',
  `base_id` BIGINT UNSIGNED NOT NULL COMMENT 'Internship base ID',
  `base_quota` INT NOT NULL DEFAULT 0 COMMENT 'Base quota',
  `sort_no` INT NOT NULL DEFAULT 1 COMMENT 'Sort number',
  `status` VARCHAR(16) NOT NULL DEFAULT 'ENABLED' COMMENT 'Status: ENABLED or DISABLED',
  `remark` VARCHAR(255) DEFAULT NULL COMMENT 'Remark',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Created by',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Updated by',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Logical delete flag',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_plan_base_plan_base` (`plan_id`, `base_id`, `deleted`),
  KEY `idx_biz_plan_base_plan_status` (`plan_id`, `status`, `deleted`),
  KEY `idx_biz_plan_base_base_id` (`base_id`),
  CONSTRAINT `fk_biz_plan_base_plan` FOREIGN KEY (`plan_id`) REFERENCES `biz_internship_plan` (`id`),
  CONSTRAINT `fk_biz_plan_base_base` FOREIGN KEY (`base_id`) REFERENCES `base_internship_base` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Plan to internship base configuration';

CREATE TABLE IF NOT EXISTS `biz_application_preference` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT 'Primary key',
  `application_id` BIGINT UNSIGNED NOT NULL COMMENT 'Application ID',
  `base_id` BIGINT UNSIGNED NOT NULL COMMENT 'Internship base ID',
  `preference_order` INT NOT NULL DEFAULT 1 COMMENT 'Preference order',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Created by',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Updated by',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Logical delete flag',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_app_pref_app_order` (`application_id`, `preference_order`, `deleted`),
  UNIQUE KEY `uk_biz_app_pref_app_base` (`application_id`, `base_id`, `deleted`),
  KEY `idx_biz_app_pref_base_id` (`base_id`),
  CONSTRAINT `fk_biz_app_pref_application` FOREIGN KEY (`application_id`) REFERENCES `biz_student_application` (`id`),
  CONSTRAINT `fk_biz_app_pref_base` FOREIGN KEY (`base_id`) REFERENCES `base_internship_base` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Application preferred internship bases';

DELETE FROM `biz_application_preference` WHERE `application_id` IN (8001, 8002, 8003, 8004, 8005);
DELETE FROM `biz_plan_base` WHERE `plan_id` IN (7001, 7002, 7003);

INSERT INTO `biz_plan_base`
(`id`, `plan_id`, `base_id`, `base_quota`, `sort_no`, `status`, `remark`, `created_by`, `updated_by`, `deleted`)
VALUES
(7401, 7001, 3301, 60, 1, 'ENABLED', '草稿计划示例基地', 5002, 5002, 0),
(7402, 7001, 3302, 60, 2, 'ENABLED', '草稿计划示例基地', 5002, 5002, 0),
(7403, 7002, 3301, 80, 1, 'ENABLED', '已发布计划示例基地', 5002, 5002, 0),
(7404, 7002, 3302, 80, 2, 'ENABLED', '已发布计划示例基地', 5002, 5002, 0),
(7405, 7003, 3301, 55, 1, 'ENABLED', '历史计划示例基地', 5002, 5002, 0),
(7406, 7003, 3302, 55, 2, 'ENABLED', '历史计划示例基地', 5002, 5002, 0);
