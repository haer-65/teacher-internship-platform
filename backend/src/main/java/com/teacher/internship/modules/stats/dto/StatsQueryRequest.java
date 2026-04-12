package com.teacher.internship.modules.stats.dto;

import lombok.Data;

@Data
public class StatsQueryRequest {

    private String dimension;
    private Long deptId;
    private Long majorId;
    private Long gradeId;
    private Long baseId;
    private Long teacherId;
    private Long planId;
    private String planStatus;
    private Boolean deptAdminScope;
}
