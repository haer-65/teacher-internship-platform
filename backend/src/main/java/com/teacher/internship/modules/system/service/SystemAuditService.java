package com.teacher.internship.modules.system.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teacher.internship.modules.system.entity.SysOperationLog;
import com.teacher.internship.modules.system.entity.SysUser;
import com.teacher.internship.modules.system.mapper.SysOperationLogMapper;
import com.teacher.internship.modules.system.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class SystemAuditService {

    private final SysOperationLogMapper operationLogMapper;
    private final SysUserMapper userMapper;
    private final ObjectMapper objectMapper;

    public SystemAuditService(SysOperationLogMapper operationLogMapper,
                              SysUserMapper userMapper,
                              ObjectMapper objectMapper) {
        this.operationLogMapper = operationLogMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    public void logSuccess(Long operatorId,
                           String moduleCode,
                           String actionCode,
                           String businessType,
                           Long businessId,
                           String requestMethod,
                           String requestUri,
                           String requestIp,
                           Object requestBody,
                           Object responseBody) {
        save(operatorId, moduleCode, actionCode, businessType, businessId, requestMethod, requestUri, requestIp,
                requestBody, responseBody, "SUCCESS", null);
    }

    public void logFail(Long operatorId,
                        String moduleCode,
                        String actionCode,
                        String businessType,
                        Long businessId,
                        String requestMethod,
                        String requestUri,
                        String requestIp,
                        Object requestBody,
                        String errorMessage) {
        save(operatorId, moduleCode, actionCode, businessType, businessId, requestMethod, requestUri, requestIp,
                requestBody, null, "FAIL", errorMessage);
    }

    private void save(Long operatorId,
                      String moduleCode,
                      String actionCode,
                      String businessType,
                      Long businessId,
                      String requestMethod,
                      String requestUri,
                      String requestIp,
                      Object requestBody,
                      Object responseBody,
                      String status,
                      String errorMessage) {
        SysOperationLog log = new SysOperationLog();
        log.setOperatorId(operatorId);
        log.setOperatorName(resolveOperatorName(operatorId));
        log.setModuleCode(trimOrDefault(moduleCode, "SYSTEM"));
        log.setActionCode(trimOrDefault(actionCode, "UNKNOWN"));
        log.setBusinessType(StringUtils.hasText(businessType) ? businessType.trim() : null);
        log.setBusinessId(businessId);
        log.setRequestMethod(StringUtils.hasText(requestMethod) ? requestMethod.trim().toUpperCase() : null);
        log.setRequestUri(StringUtils.hasText(requestUri) ? requestUri.trim() : null);
        log.setRequestIp(StringUtils.hasText(requestIp) ? requestIp.trim() : null);
        log.setRequestParams(toJsonSafe(requestBody));
        log.setResponseData(toJsonSafe(responseBody));
        log.setOperationStatus(trimOrDefault(status, "SUCCESS"));
        log.setErrorMessage(StringUtils.hasText(errorMessage) ? errorMessage.trim() : null);
        log.setOperateTime(LocalDateTime.now());
        log.setCreatedBy(operatorId == null ? 0L : operatorId);
        log.setUpdatedBy(operatorId == null ? 0L : operatorId);
        log.setDeleted(0L);
        operationLogMapper.insert(log);
    }

    private String resolveOperatorName(Long operatorId) {
        if (operatorId == null) {
            return "系统";
        }
        SysUser user = userMapper.selectById(operatorId);
        if (user == null) {
            return String.valueOf(operatorId);
        }
        if (StringUtils.hasText(user.getRealName())) {
            return user.getRealName().trim();
        }
        if (StringUtils.hasText(user.getStudentNo())) {
            return user.getStudentNo().trim();
        }
        if (StringUtils.hasText(user.getTeacherNo())) {
            return user.getTeacherNo().trim();
        }
        if (StringUtils.hasText(user.getLoginName())) {
            return user.getLoginName().trim();
        }
        return String.valueOf(operatorId);
    }

    private String toJsonSafe(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof String) {
            return (String) value;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ex) {
            return "{\"error\":\"序列化失败\"}";
        }
    }

    private String trimOrDefault(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }
}
