-- =============================================
-- Evaluation management extension
-- 1) Allow multiple guidance records on same material version
-- 2) Support final evaluation not bound to material version
-- 3) Support itemized scores json and traceable record sequence
-- =============================================

USE `teacher_internship_platform`;

-- ensure FK-safe index exists before dropping old unique index
SET @add_idx_material_version_fk_sql = (
  SELECT IF(COUNT(1) = 0,
            'ALTER TABLE `biz_evaluation` ADD INDEX `idx_biz_evaluation_material_version_fk` (`material_version_id`)',
            'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_evaluation'
    AND index_name = 'idx_biz_evaluation_material_version_fk'
);
PREPARE stmt_add_idx_material_version_fk FROM @add_idx_material_version_fk_sql;
EXECUTE stmt_add_idx_material_version_fk;
DEALLOCATE PREPARE stmt_add_idx_material_version_fk;

-- drop old uniqueness constraint that blocks multiple process records
SET @drop_idx_sql = (
  SELECT IF(COUNT(1) > 0,
            'ALTER TABLE `biz_evaluation` DROP INDEX `uk_biz_evaluation_version_evaluator_type`',
            'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_evaluation'
    AND index_name = 'uk_biz_evaluation_version_evaluator_type'
);
PREPARE stmt_drop_idx FROM @drop_idx_sql;
EXECUTE stmt_drop_idx;
DEALLOCATE PREPARE stmt_drop_idx;

-- make material binding nullable for FINAL evaluations
ALTER TABLE `biz_evaluation`
  MODIFY COLUMN `material_id` BIGINT UNSIGNED NULL COMMENT '材料主表ID（PROCESS 必填，FINAL 可空）',
  MODIFY COLUMN `material_version_id` BIGINT UNSIGNED NULL COMMENT '材料版本ID（PROCESS 必填，FINAL 可空）';

-- add record sequence and score items json
SET @add_record_no_sql = (
  SELECT IF(COUNT(1) = 0,
            'ALTER TABLE `biz_evaluation` ADD COLUMN `record_no` INT NOT NULL DEFAULT 1 COMMENT ''同一评价范围内记录序号'' AFTER `evaluation_type`',
            'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_evaluation'
    AND column_name = 'record_no'
);
PREPARE stmt_add_record_no FROM @add_record_no_sql;
EXECUTE stmt_add_record_no;
DEALLOCATE PREPARE stmt_add_record_no;

SET @add_score_items_sql = (
  SELECT IF(COUNT(1) = 0,
            'ALTER TABLE `biz_evaluation` ADD COLUMN `score_items_json` TEXT NULL COMMENT ''分项评分 JSON'' AFTER `score`',
            'SELECT 1')
  FROM information_schema.columns
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_evaluation'
    AND column_name = 'score_items_json'
);
PREPARE stmt_add_score_items FROM @add_score_items_sql;
EXECUTE stmt_add_score_items;
DEALLOCATE PREPARE stmt_add_score_items;

-- ensure indexes for query and traceability
SET @add_idx_type_time_sql = (
  SELECT IF(COUNT(1) = 0,
            'ALTER TABLE `biz_evaluation` ADD INDEX `idx_biz_evaluation_type_time` (`evaluation_type`, `evaluated_time`)',
            'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_evaluation'
    AND index_name = 'idx_biz_evaluation_type_time'
);
PREPARE stmt_add_idx_type_time FROM @add_idx_type_time_sql;
EXECUTE stmt_add_idx_type_time;
DEALLOCATE PREPARE stmt_add_idx_type_time;

SET @add_idx_eval_scope_sql = (
  SELECT IF(COUNT(1) = 0,
            'ALTER TABLE `biz_evaluation` ADD INDEX `idx_biz_evaluation_scope` (`assignment_id`, `evaluator_id`, `evaluation_type`, `material_version_id`, `record_no`)',
            'SELECT 1')
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'biz_evaluation'
    AND index_name = 'idx_biz_evaluation_scope'
);
PREPARE stmt_add_idx_eval_scope FROM @add_idx_eval_scope_sql;
EXECUTE stmt_add_idx_eval_scope;
DEALLOCATE PREPARE stmt_add_idx_eval_scope;
