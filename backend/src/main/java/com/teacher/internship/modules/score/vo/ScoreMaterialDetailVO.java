package com.teacher.internship.modules.score.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreMaterialDetailVO {

    private Long materialTypeId;
    private String materialTypeCode;
    private String materialTypeName;
    private BigDecimal materialWeight;

    private Long materialId;
    private Long materialVersionId;
    private Integer materialVersionNo;
    private String fileName;

    private BigDecimal innerTeacherProcessScore;
    private BigDecimal baseTeacherProcessScore;
    private BigDecimal materialCompositeScore;
    private BigDecimal weightedContribution;
}
