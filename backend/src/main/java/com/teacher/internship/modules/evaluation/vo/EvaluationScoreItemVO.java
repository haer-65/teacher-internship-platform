package com.teacher.internship.modules.evaluation.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class EvaluationScoreItemVO {

    private String itemName;
    private BigDecimal itemScore;
    private BigDecimal itemWeight;
}
