package com.teacher.internship.modules.system.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.teacher.internship.modules.system.entity.SysLoginLog;
import com.teacher.internship.modules.system.entity.SysOperationLog;
import com.teacher.internship.modules.system.mapper.SysLoginLogMapper;
import com.teacher.internship.modules.system.mapper.SysOperationLogMapper;
import com.teacher.internship.modules.system.vo.PageResultVO;
import com.teacher.internship.modules.system.vo.SysLoginLogItemVO;
import com.teacher.internship.modules.system.vo.SysOperationLogItemVO;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class SystemLogService {

    private final SysOperationLogMapper operationLogMapper;
    private final SysLoginLogMapper loginLogMapper;

    public SystemLogService(SysOperationLogMapper operationLogMapper,
                            SysLoginLogMapper loginLogMapper) {
        this.operationLogMapper = operationLogMapper;
        this.loginLogMapper = loginLogMapper;
    }

    public PageResultVO<SysOperationLogItemVO> queryOperationPage(long page,
                                                                  long size,
                                                                  String keyword,
                                                                  String moduleCode,
                                                                  String actionCode,
                                                                  String operationStatus,
                                                                  LocalDateTime startTime,
                                                                  LocalDateTime endTime) {
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);
        LambdaQueryWrapper<SysOperationLog> wrapper = new LambdaQueryWrapper<SysOperationLog>()
                .eq(SysOperationLog::getDeleted, 0L)
                .orderByDesc(SysOperationLog::getOperateTime)
                .orderByDesc(SysOperationLog::getId);

        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysOperationLog::getOperatorName, keyword)
                    .or().like(SysOperationLog::getRequestUri, keyword)
                    .or().like(SysOperationLog::getBusinessType, keyword));
        }
        if (StringUtils.hasText(moduleCode)) {
            wrapper.eq(SysOperationLog::getModuleCode, normalizeCode(moduleCode));
        }
        if (StringUtils.hasText(actionCode)) {
            wrapper.eq(SysOperationLog::getActionCode, normalizeCode(actionCode));
        }
        if (StringUtils.hasText(operationStatus)) {
            wrapper.eq(SysOperationLog::getOperationStatus, normalizeCode(operationStatus));
        }
        if (startTime != null) {
            wrapper.ge(SysOperationLog::getOperateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysOperationLog::getOperateTime, endTime);
        }

        Page<SysOperationLog> pageResult = operationLogMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        PageResultVO<SysOperationLogItemVO> result = new PageResultVO<>();
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal(pageResult.getTotal());
        result.setRecords(pageResult.getRecords().stream().map(this::toOperationVO).collect(Collectors.toList()));
        return result;
    }

    public PageResultVO<SysLoginLogItemVO> queryLoginPage(long page,
                                                          long size,
                                                          String keyword,
                                                          String loginResult,
                                                          LocalDateTime startTime,
                                                          LocalDateTime endTime) {
        long safePage = normalizePage(page);
        long safeSize = normalizeSize(size);

        LambdaQueryWrapper<SysLoginLog> wrapper = new LambdaQueryWrapper<SysLoginLog>()
                .eq(SysLoginLog::getDeleted, 0L)
                .orderByDesc(SysLoginLog::getLoginTime)
                .orderByDesc(SysLoginLog::getId);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(SysLoginLog::getLoginName, keyword)
                    .or().like(SysLoginLog::getLoginIp, keyword));
        }
        if (StringUtils.hasText(loginResult)) {
            wrapper.eq(SysLoginLog::getLoginResult, normalizeCode(loginResult));
        }
        if (startTime != null) {
            wrapper.ge(SysLoginLog::getLoginTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(SysLoginLog::getLoginTime, endTime);
        }

        Page<SysLoginLog> pageResult = loginLogMapper.selectPage(new Page<>(safePage, safeSize), wrapper);
        PageResultVO<SysLoginLogItemVO> result = new PageResultVO<>();
        result.setPage(safePage);
        result.setSize(safeSize);
        result.setTotal(pageResult.getTotal());
        result.setRecords(pageResult.getRecords().stream().map(this::toLoginVO).collect(Collectors.toList()));
        return result;
    }

    private SysOperationLogItemVO toOperationVO(SysOperationLog entity) {
        SysOperationLogItemVO vo = new SysOperationLogItemVO();
        vo.setId(entity.getId());
        vo.setOperatorId(entity.getOperatorId());
        vo.setOperatorName(entity.getOperatorName());
        vo.setModuleCode(entity.getModuleCode());
        vo.setActionCode(entity.getActionCode());
        vo.setBusinessType(entity.getBusinessType());
        vo.setBusinessId(entity.getBusinessId());
        vo.setRequestMethod(entity.getRequestMethod());
        vo.setRequestUri(entity.getRequestUri());
        vo.setRequestIp(entity.getRequestIp());
        vo.setRequestParams(entity.getRequestParams());
        vo.setResponseData(entity.getResponseData());
        vo.setOperationStatus(entity.getOperationStatus());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setOperateTime(entity.getOperateTime());
        return vo;
    }

    private SysLoginLogItemVO toLoginVO(SysLoginLog entity) {
        SysLoginLogItemVO vo = new SysLoginLogItemVO();
        vo.setId(entity.getId());
        vo.setAccountNo(entity.getLoginName());
        vo.setUserId(entity.getUserId());
        vo.setLoginIp(entity.getLoginIp());
        vo.setUserAgent(entity.getUserAgent());
        vo.setLoginResult(entity.getLoginResult());
        vo.setFailReason(entity.getFailReason());
        vo.setLoginTime(entity.getLoginTime());
        return vo;
    }

    private long normalizePage(long page) {
        return page <= 0 ? 1 : page;
    }

    private long normalizeSize(long size) {
        if (size <= 0) {
            return 10;
        }
        return Math.min(size, 200);
    }

    private String normalizeCode(String text) {
        return text == null ? "" : text.trim().toUpperCase(Locale.ROOT);
    }
}
