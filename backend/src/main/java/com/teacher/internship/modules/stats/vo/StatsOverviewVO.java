package com.teacher.internship.modules.stats.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsOverviewVO {

    private long internshipStudentCount;
    private long materialTotalCount;
    private long materialSubmittedCount;
    private long materialOverdueCount;
    private BigDecimal materialSubmitRate;
    private BigDecimal materialOverdueRate;
    private long assignmentTotalCount;
    private long evaluationCompletedCount;
    private BigDecimal evaluationCompletionRate;
}
