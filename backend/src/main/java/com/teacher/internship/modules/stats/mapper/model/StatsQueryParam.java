package com.teacher.internship.modules.stats.mapper.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatsQueryParam {

    private String dimension;
    private Long deptId;
    private Long majorId;
    private Long gradeId;
    private Long baseId;
    private Long teacherId;
    private Long planId;
    private String planStatus;
    private Boolean deptAdminScope;
    private LocalDateTime now;
    private Long limit;
    private Long offset;
}
