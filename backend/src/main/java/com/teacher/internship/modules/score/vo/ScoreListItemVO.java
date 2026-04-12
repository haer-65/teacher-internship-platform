package com.teacher.internship.modules.score.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class ScoreListItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;
    private String planCode;
    private String planName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assignmentId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String deptName;
    private String majorName;
    private String gradeName;

    private BigDecimal processScore;
    private BigDecimal finalScore;
    private BigDecimal autoTotalScore;
    private BigDecimal totalScore;

    private String status;
    private Integer adjustedFlag;
    private Integer adjustTimes;
    private String adjustReason;
    private String adjustByName;
    private LocalDateTime adjustTime;
    private LocalDateTime publishedTime;
    private LocalDateTime lastCalcTime;
}
