package com.teacher.internship.modules.evaluation.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TeacherFinalPendingItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long assignmentId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;
    private String planCode;
    private String planName;
    private String planStatus;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;
    private String studentNo;
    private String studentName;

    private Integer myEvaluationCount;
    private LocalDateTime myLatestEvaluatedTime;
    private BigDecimal myLatestScore;
    private Boolean canEvaluate;
    private Boolean pending;
}
