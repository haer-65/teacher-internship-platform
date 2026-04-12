package com.teacher.internship.modules.evaluation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("biz_evaluation")
@EqualsAndHashCode(callSuper = true)
public class BizEvaluation extends BaseEntity {

    private Long planId;
    private Long assignmentId;
    private Long studentId;
    private Long materialId;
    private Long materialVersionId;
    private Long evaluatorId;
    private String evaluatorRole;
    private String evaluationType;
    private Integer recordNo;
    private BigDecimal score;
    private String scoreItemsJson;
    private String commentText;
    private LocalDateTime evaluatedTime;
}
