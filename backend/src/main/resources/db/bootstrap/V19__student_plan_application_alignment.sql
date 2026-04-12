USE `teacher_internship_platform`;

-- 统一演示计划数据，避免学生计划页与申请页出现“能看到计划但无法申请”的错觉
-- 7001 保持草稿态，不应出现在学生端
UPDATE `biz_internship_plan`
SET `plan_status` = 'DRAFT',
    `published_time` = NULL,
    `archived_time` = NULL,
    `updated_by` = 0
WHERE `id` = 7001
  AND `deleted` = 0;

-- 7002 保持为当前可申请的已发布计划
UPDATE `biz_internship_plan`
SET `plan_status` = 'PUBLISHED',
    `published_time` = COALESCE(`published_time`, '2026-03-20 10:00:00'),
    `archived_time` = NULL,
    `apply_deadline` = '2026-05-15 23:59:59',
    `updated_by` = 0
WHERE `id` = 7002
  AND `deleted` = 0;

-- 7003 作为历史归档计划保留
UPDATE `biz_internship_plan`
SET `plan_status` = 'ARCHIVED',
    `archived_time` = COALESCE(`archived_time`, '2026-01-05 09:00:00'),
    `updated_by` = 0
WHERE `id` = 7003
  AND `deleted` = 0;
