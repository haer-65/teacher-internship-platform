package com.teacher.internship.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.teacher.internship.modules.system.entity.SysLoginLog;
import com.teacher.internship.modules.system.entity.SysOperationLog;
import com.teacher.internship.modules.system.mapper.SysLoginLogMapper;
import com.teacher.internship.modules.system.mapper.SysOperationLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SystemLogCleanupService {

    private static final int DEFAULT_BATCH_SIZE = 1000;

    private final SysLoginLogMapper loginLogMapper;
    private final SysOperationLogMapper operationLogMapper;

    public SystemLogCleanupService(SysLoginLogMapper loginLogMapper,
                                   SysOperationLogMapper operationLogMapper) {
        this.loginLogMapper = loginLogMapper;
        this.operationLogMapper = operationLogMapper;
    }

    public CleanupResult cleanupExpiredLogs(LocalDateTime thresholdTime) {
        if (thresholdTime == null) {
            return CleanupResult.empty();
        }
        long deletedLoginLogs = cleanupLoginLogs(thresholdTime);
        long deletedOperationLogs = cleanupOperationLogs(thresholdTime);
        return new CleanupResult(deletedLoginLogs, deletedOperationLogs);
    }

    private long cleanupLoginLogs(LocalDateTime thresholdTime) {
        long deleted = 0L;
        while (true) {
            List<Long> ids = loginLogMapper.selectList(new LambdaQueryWrapper<SysLoginLog>()
                            .select(SysLoginLog::getId)
                            .eq(SysLoginLog::getDeleted, 0L)
                            .lt(SysLoginLog::getLoginTime, thresholdTime)
                            .orderByAsc(SysLoginLog::getLoginTime)
                            .orderByAsc(SysLoginLog::getId)
                            .last("LIMIT " + DEFAULT_BATCH_SIZE))
                    .stream()
                    .map(SysLoginLog::getId)
                    .toList();
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            deleted += loginLogMapper.deleteBatchIds(ids);
        }
        return deleted;
    }

    private long cleanupOperationLogs(LocalDateTime thresholdTime) {
        long deleted = 0L;
        while (true) {
            List<Long> ids = operationLogMapper.selectList(new LambdaQueryWrapper<SysOperationLog>()
                            .select(SysOperationLog::getId)
                            .eq(SysOperationLog::getDeleted, 0L)
                            .lt(SysOperationLog::getOperateTime, thresholdTime)
                            .orderByAsc(SysOperationLog::getOperateTime)
                            .orderByAsc(SysOperationLog::getId)
                            .last("LIMIT " + DEFAULT_BATCH_SIZE))
                    .stream()
                    .map(SysOperationLog::getId)
                    .toList();
            if (CollectionUtils.isEmpty(ids)) {
                break;
            }
            deleted += operationLogMapper.deleteBatchIds(ids);
        }
        return deleted;
    }

    public record CleanupResult(long deletedLoginLogs, long deletedOperationLogs) {
        public static CleanupResult empty() {
            return new CleanupResult(0L, 0L);
        }

        public long totalDeleted() {
            return deletedLoginLogs + deletedOperationLogs;
        }
    }
}
