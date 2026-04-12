-- =============================================
-- Menu component path alignment for frontend route skeleton
-- Keep route_path unchanged, only align component_path naming
-- =============================================

USE `teacher_internship_platform`;

UPDATE `sys_menu`
SET `component_path` = 'views/plan/PlanEntryView'
WHERE `id` = 2002 AND `deleted` = 0;

UPDATE `sys_menu`
SET `component_path` = 'views/application/ApplicationEntryView'
WHERE `id` = 2003 AND `deleted` = 0;

UPDATE `sys_menu`
SET `component_path` = 'views/material/MaterialEntryView'
WHERE `id` = 2004 AND `deleted` = 0;

UPDATE `sys_menu`
SET `component_path` = 'views/evaluation/EvaluationEntryView'
WHERE `id` = 2005 AND `deleted` = 0;

UPDATE `sys_menu`
SET `component_path` = 'views/score/ScoreEntryView'
WHERE `id` = 2006 AND `deleted` = 0;

UPDATE `sys_menu`
SET `component_path` = 'views/stats/StatsAnalysisView'
WHERE `id` = 2008 AND `deleted` = 0;

UPDATE `sys_menu`
SET `component_path` = 'views/system/SystemManagementView'
WHERE `id` = 2009 AND `deleted` = 0;
