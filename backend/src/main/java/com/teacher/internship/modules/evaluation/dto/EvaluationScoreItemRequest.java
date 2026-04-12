package com.teacher.internship.modules.evaluation.dto;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class EvaluationScoreItemRequest {

    @NotBlank(message = "评分项名称不能为空")
    private String itemName;

    @DecimalMin(value = "0.00", message = "评分项得分不能小于 0")
    @DecimalMax(value = "100.00", message = "评分项得分不能大于 100")
    private BigDecimal itemScore;

    @DecimalMin(value = "0.00", message = "评分项权重不能小于 0")
    @DecimalMax(value = "100.00", message = "评分项权重不能大于 100")
    private BigDecimal itemWeight;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public BigDecimal getItemScore() {
        return itemScore;
    }

    public void setItemScore(BigDecimal itemScore) {
        this.itemScore = itemScore;
    }

    public BigDecimal getItemWeight() {
        return itemWeight;
    }

    public void setItemWeight(BigDecimal itemWeight) {
        this.itemWeight = itemWeight;
    }
}
