package com.teacher.internship.modules.score.dto;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

public class ScoreAdjustRequest {

    @NotNull(message = "调整后总分不能为空")
    @DecimalMin(value = "0.00", message = "调整后总分不能小于 0")
    @DecimalMax(value = "100.00", message = "调整后总分不能大于 100")
    private BigDecimal newTotalScore;

    @NotBlank(message = "调整原因不能为空")
    private String adjustReason;

    public BigDecimal getNewTotalScore() {
        return newTotalScore;
    }

    public void setNewTotalScore(BigDecimal newTotalScore) {
        this.newTotalScore = newTotalScore;
    }

    public String getAdjustReason() {
        return adjustReason;
    }

    public void setAdjustReason(String adjustReason) {
        this.adjustReason = adjustReason;
    }
}
