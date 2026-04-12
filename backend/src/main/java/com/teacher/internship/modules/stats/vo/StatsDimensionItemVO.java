package com.teacher.internship.modules.stats.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StatsDimensionItemVO {

    private String dimensionCode;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long dimensionId;
    private String dimensionName;

    private long internshipStudentCount;
    private long materialTotalCount;
    private long materialSubmittedCount;
    private long materialOverdueCount;
    private BigDecimal materialSubmitRate;
    private BigDecimal materialOverdueRate;

    private long assignmentTotalCount;
    private long evaluationCompletedCount;
    private BigDecimal evaluationCompletionRate;

    private long scoreStudentCount;
    private BigDecimal averageScore;
    private BigDecimal excellentRate;
    private BigDecimal passRate;
}
