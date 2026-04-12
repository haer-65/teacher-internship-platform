-- =============================================
-- Material management extension:
-- 1) late resubmit control table
-- 2) 院系管理员上传/管理权限补齐
-- =============================================

USE `teacher_internship_platform`;

DROP TABLE IF EXISTS `biz_material_resubmit_control`;

CREATE TABLE `biz_material_resubmit_control` (
  `id` BIGINT UNSIGNED NOT NULL COMMENT 'Primary key ID',
  `material_id` BIGINT UNSIGNED NOT NULL COMMENT 'Material ID',
  `open_flag` TINYINT(1) NOT NULL DEFAULT 1 COMMENT 'Late submit open flag: 1-open, 0-close',
  `open_until` DATETIME DEFAULT NULL COMMENT 'Late submit end time, null means no end time',
  `open_reason` VARCHAR(500) DEFAULT NULL COMMENT 'Open reason',
  `opened_by` BIGINT UNSIGNED NOT NULL COMMENT 'Operator user ID when opening',
  `opened_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Open time',
  `closed_by` BIGINT UNSIGNED DEFAULT NULL COMMENT 'Operator user ID when closing',
  `closed_time` DATETIME DEFAULT NULL COMMENT 'Close time',
  `created_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Created by',
  `created_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `updated_by` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Updated by',
  `updated_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  `deleted` BIGINT UNSIGNED NOT NULL DEFAULT 0 COMMENT 'Logical delete flag: 0-not deleted, >0 delete timestamp',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_biz_material_resubmit_material` (`material_id`, `deleted`),
  KEY `idx_biz_material_resubmit_open` (`open_flag`, `open_until`),
  CONSTRAINT `fk_biz_material_resubmit_material` FOREIGN KEY (`material_id`) REFERENCES `biz_material` (`id`),
  CONSTRAINT `fk_biz_material_resubmit_opened_by` FOREIGN KEY (`opened_by`) REFERENCES `sys_user` (`id`),
  CONSTRAINT `fk_biz_material_resubmit_closed_by` FOREIGN KEY (`closed_by`) REFERENCES `sys_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Material late resubmit control table';

-- 允许院系管理员使用材料管理按钮权限
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `created_by`, `updated_by`)
SELECT 300068, 1004, 2113, 0, 0
WHERE NOT EXISTS (
  SELECT 1 FROM `sys_role_menu`
  WHERE `role_id` = 1004 AND `menu_id` = 2113 AND `deleted` = 0
);
