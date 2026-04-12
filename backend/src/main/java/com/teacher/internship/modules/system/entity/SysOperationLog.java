package com.teacher.internship.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_operation_log")
@EqualsAndHashCode(callSuper = true)
public class SysOperationLog extends BaseEntity {

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

