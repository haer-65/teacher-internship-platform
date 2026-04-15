package com.teacher.internship.modules.system.schedule;

import com.teacher.internship.modules.system.service.SystemLogCleanupService;
import com.teacher.internship.modules.system.service.SystemParamService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class SystemLogCleanupScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SystemLogCleanupScheduler.class);
    private static final String PARAM_LOG_RETENTION_DAYS = "LOG_RETENTION_DAYS";
    private static final int DEFAULT_RETENTION_DAYS = 30;

    private final SystemParamService paramService;
    private final SystemLogCleanupService cleanupService;

    public SystemLogCleanupScheduler(SystemParamService paramService,
                                     SystemLogCleanupService cleanupService) {
        this.paramService = paramService;
        this.cleanupService = cleanupService;
    }

    @Scheduled(cron = "${app.system.log-cleanup-cron:0 30 2 * * ?}")
    public void scheduleCleanup() {
        int retentionDays = resolveRetentionDays();
        LocalDateTime thresholdTime = LocalDateTime.now().minusDays(retentionDays);
        SystemLogCleanupService.CleanupResult result = cleanupService.cleanupExpiredLogs(thresholdTime);
        if (result.totalDeleted() > 0) {
            LOGGER.info("Cleaned up {} login log(s) and {} operation log(s) older than {} days.",
                    result.deletedLoginLogs(), result.deletedOperationLogs(), retentionDays);
        }
    }

    private int resolveRetentionDays() {
        int configuredDays = paramService.getIntValue(PARAM_LOG_RETENTION_DAYS, DEFAULT_RETENTION_DAYS);
        return Math.max(configuredDays, 1);
    }
}
