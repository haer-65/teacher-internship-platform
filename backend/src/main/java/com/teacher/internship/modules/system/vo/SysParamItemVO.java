package com.teacher.internship.modules.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysParamItemVO {

    private Long id;
    private String paramCode;
    private String paramName;
    private String paramValue;
    private String paramType;
    private String status;
    private String remark;
    private LocalDateTime updatedTime;
}

