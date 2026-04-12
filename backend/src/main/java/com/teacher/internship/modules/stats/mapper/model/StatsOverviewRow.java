package com.teacher.internship.modules.stats.mapper.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsOverviewRow {

    private Long internshipStudentCount;
    private Long materialTotalCount;
    private Long materialSubmittedCount;
    private Long materialOverdueCount;
    private Long assignmentTotalCount;
    private Long evaluationCompletedCount;
    private Long scoreStudentCount;
    private BigDecimal averageScore;
    private Long excellentCount;
    private Long passCount;
}
