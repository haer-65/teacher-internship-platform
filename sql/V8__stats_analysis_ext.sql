-- =============================================
-- Statistics analysis extension
-- 1) Add indexes for aggregation scan paths
-- 2) Keep existing schema compatibility
-- =============================================

USE `teacher_internship_platform`;

SET @add_idx_material_assignment_status_sql = (
  SELECT IF(COUNT(1) = 0,
            'ALTER TABLE `biz_material` ADD INDEX `idx_biz_material_assignment_status_deleted` (`assignment_id`, `material_status`, `deleted`)',
            'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_material'
    AND index_name = 'idx_biz_material_assignment_status_deleted'
);
PREPARE stmt_add_idx_material_assignment_status FROM @add_idx_material_assignment_status_sql;
EXECUTE stmt_add_idx_material_assignment_status;
DEALLOCATE PREPARE stmt_add_idx_material_assignment_status;

SET @add_idx_score_assignment_deleted_sql = (
  SELECT IF(COUNT(1) = 0,
            'ALTER TABLE `biz_score_sheet` ADD INDEX `idx_biz_score_assignment_deleted` (`assignment_id`, `deleted`)',
            'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_score_sheet'
    AND index_name = 'idx_biz_score_assignment_deleted'
);
PREPARE stmt_add_idx_score_assignment_deleted FROM @add_idx_score_assignment_deleted_sql;
EXECUTE stmt_add_idx_score_assignment_deleted;
DEALLOCATE PREPARE stmt_add_idx_score_assignment_deleted;

SET @add_idx_assignment_scope_filter_sql = (
  SELECT IF(COUNT(1) = 0,
            'ALTER TABLE `biz_assignment` ADD INDEX `idx_biz_assignment_scope_filter` (`is_current`, `plan_id`, `base_id`, `deleted`)',
            'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_assignment'
    AND index_name = 'idx_biz_assignment_scope_filter'
);
PREPARE stmt_add_idx_assignment_scope_filter FROM @add_idx_assignment_scope_filter_sql;
EXECUTE stmt_add_idx_assignment_scope_filter;
DEALLOCATE PREPARE stmt_add_idx_assignment_scope_filter;
