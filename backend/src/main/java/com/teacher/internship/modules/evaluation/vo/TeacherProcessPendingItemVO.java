package com.teacher.internship.modules.evaluation.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TeacherProcessPendingItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long materialId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long materialVersionId;
    private Integer materialVersionNo;
    private String fileName;
    private LocalDateTime submittedTime;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long assignmentId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;
    private String planCode;
    private String planName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;
    private String studentNo;
    private String studentName;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long materialTypeId;
    private String materialTypeCode;
    private String materialTypeName;

    private Integer myEvaluationCount;
    private LocalDateTime myLatestEvaluatedTime;
    private BigDecimal myLatestScore;
    private Boolean pending;
}
