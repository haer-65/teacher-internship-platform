-- =============================================
-- Score management extension
-- 1) Keep auto-calculated total score snapshot
-- 2) Keep score detail snapshot for traceability
-- =============================================

USE `teacher_internship_platform`;

SET @add_auto_total_score_sql = (
  SELECT IF(COUNT(1) = 0,
            'ALTER TABLE `biz_score_sheet` ADD COLUMN `auto_total_score` DECIMAL(5,2) NOT NULL DEFAULT 0.00 COMMENT ''Auto calculated total score before manual adjustment'' AFTER `final_score`',
            'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_score_sheet'
    AND column_name = 'auto_total_score'
);
PREPARE stmt_add_auto_total_score FROM @add_auto_total_score_sql;
EXECUTE stmt_add_auto_total_score;
DEALLOCATE PREPARE stmt_add_auto_total_score;

SET @add_detail_json_sql = (
  SELECT IF(COUNT(1) = 0,
            'ALTER TABLE `biz_score_sheet` ADD COLUMN `detail_json` LONGTEXT NULL COMMENT ''Score detail snapshot json'' AFTER `total_score`',
            'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_score_sheet'
    AND column_name = 'detail_json'
);
PREPARE stmt_add_detail_json FROM @add_detail_json_sql;
EXECUTE stmt_add_detail_json;
DEALLOCATE PREPARE stmt_add_detail_json;

SET @add_last_calc_time_sql = (
  SELECT IF(COUNT(1) = 0,
            'ALTER TABLE `biz_score_sheet` ADD COLUMN `last_calc_time` DATETIME NULL COMMENT ''Last recalculation time'' AFTER `detail_json`',
            'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_score_sheet'
    AND column_name = 'last_calc_time'
);
PREPARE stmt_add_last_calc_time FROM @add_last_calc_time_sql;
EXECUTE stmt_add_last_calc_time;
DEALLOCATE PREPARE stmt_add_last_calc_time;

SET @add_idx_plan_status_sql = (
  SELECT IF(COUNT(1) = 0,
            'ALTER TABLE `biz_score_sheet` ADD INDEX `idx_biz_score_plan_status` (`plan_id`, `status`)',
            'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_score_sheet'
    AND index_name = 'idx_biz_score_plan_status'
);
PREPARE stmt_add_idx_plan_status FROM @add_idx_plan_status_sql;
EXECUTE stmt_add_idx_plan_status;
DEALLOCATE PREPARE stmt_add_idx_plan_status;
