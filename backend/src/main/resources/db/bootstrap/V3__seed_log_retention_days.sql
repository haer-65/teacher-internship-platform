INSERT INTO `sys_param` (`id`, `param_code`, `param_name`, `param_value`, `param_type`, `status`, `remark`, `created_by`, `updated_by`)
SELECT 6006, 'LOG_RETENTION_DAYS', '日志保留天数', '30', 'SYSTEM', 'ENABLED', '自动清理日志保留周期', 0, 0
FROM DUAL
WHERE NOT EXISTS (
    SELECT 1
    FROM `sys_param`
    WHERE `param_code` = 'LOG_RETENTION_DAYS'
      AND `deleted` = 0
);
