package com.teacher.internship.modules.score.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

@Data
public class ScoreDetailVO {

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

    private BigDecimal innerTeacherWeight;
    private BigDecimal baseTeacherWeight;

    private BigDecimal processScore;
    private BigDecimal finalScore;
    private BigDecimal autoTotalScore;
    private BigDecimal totalScore;

    private BigDecimal innerTeacherFinalScore;
    private BigDecimal baseTeacherFinalScore;

    private String formulaText;
    private String status;
    private Integer adjustedFlag;
    private Integer adjustTimes;
    private String adjustReason;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long adjustBy;
    private String adjustByName;
    private LocalDateTime adjustTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long publishedBy;
    private String publishedByName;
    private LocalDateTime publishedTime;
    private LocalDateTime lastCalcTime;

    private List<ScoreMaterialDetailVO> materialDetails = new ArrayList<>();
}
