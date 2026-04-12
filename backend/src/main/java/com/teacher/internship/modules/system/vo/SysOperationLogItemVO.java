package com.teacher.internship.modules.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysOperationLogItemVO {

    private Long id;
    private Long operatorId;
    private String operatorName;
    private String moduleCode;
    private String actionCode;
    private String businessType;
    private Long businessId;
    private String requestMethod;
    private String requestUri;
    private String requestIp;
    private String requestParams;
    private String responseData;
    private String operationStatus;
    private String errorMessage;
    private LocalDateTime operateTime;
}

