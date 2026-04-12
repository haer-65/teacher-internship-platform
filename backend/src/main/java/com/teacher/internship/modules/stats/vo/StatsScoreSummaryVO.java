package com.teacher.internship.modules.stats.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsScoreSummaryVO {

    private long scoreStudentCount;
    private BigDecimal averageScore;
    private BigDecimal excellentRate;
    private BigDecimal passRate;
}
