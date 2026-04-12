package com.teacher.internship.modules.evaluation.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EvaluationScoreSummaryVO {

    private Long assignmentId;
    private Long planId;
    private Long studentId;

    private BigDecimal innerTeacherProcessAvg;
    private BigDecimal baseTeacherProcessAvg;

    private BigDecimal innerTeacherFinalLatest;
    private BigDecimal baseTeacherFinalLatest;

    private BigDecimal processCompositeAvg;
    private BigDecimal finalCompositeAvg;
}
