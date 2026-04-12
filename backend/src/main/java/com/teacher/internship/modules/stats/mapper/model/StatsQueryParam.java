package com.teacher.internship.modules.stats.mapper.model;

import lombok.Data;

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
    private Long limit;
    private Long offset;
}
