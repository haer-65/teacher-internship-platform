package com.teacher.internship.modules.score.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_score_sheet")
@EqualsAndHashCode(callSuper = true)
public class BizScoreSheet extends BaseEntity {

    private Long planId;
    private Long assignmentId;
    private Long studentId;
    private BigDecimal processScore;
    private BigDecimal finalScore;
    private BigDecimal autoTotalScore;
    private BigDecimal totalScore;
    private String detailJson;
    private LocalDateTime lastCalcTime;
    private String status;
    private Integer adjustedFlag;
    private String adjustReason;
    private Long adjustBy;
    private LocalDateTime adjustTime;
    private Integer adjustTimes;
    private Long publishedBy;
    private LocalDateTime publishedTime;
}
