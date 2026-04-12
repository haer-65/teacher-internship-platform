package com.teacher.internship.modules.evaluation.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class EvaluationRecordVO {

    private Long id;
    private Long planId;
    private String planCode;
    private String planName;
    private Long assignmentId;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private Long materialId;
    private Long materialVersionId;
    private Integer materialVersionNo;
    private String materialTypeCode;
    private String materialTypeName;
    private String fileName;
    private Long evaluatorId;
    private String evaluatorName;
    private String evaluatorRole;
    private String evaluationType;
    private Integer recordNo;
    private BigDecimal score;
    private String commentText;
    private LocalDateTime evaluatedTime;
    private List<EvaluationScoreItemVO> scoreItems = new ArrayList<>();
}
