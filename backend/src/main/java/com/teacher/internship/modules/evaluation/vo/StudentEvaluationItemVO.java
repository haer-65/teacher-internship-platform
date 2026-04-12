package com.teacher.internship.modules.evaluation.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StudentEvaluationItemVO {

    private Long id;
    private String evaluationType;
    private Integer recordNo;

    private Long assignmentId;
    private Long planId;
    private String planCode;
    private String planName;

    private Long materialId;
    private Long materialVersionId;
    private Integer materialVersionNo;
    private String materialTypeName;

    private Long evaluatorId;
    private String evaluatorName;
    private String evaluatorRole;

    private BigDecimal score;
    private String commentText;
    private LocalDateTime evaluatedTime;
}
