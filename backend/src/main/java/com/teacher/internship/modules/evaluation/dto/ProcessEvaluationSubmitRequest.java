package com.teacher.internship.modules.evaluation.dto;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProcessEvaluationSubmitRequest {

    @NotNull(message = "材料版本标识不能为空")
    private Long materialVersionId;

    @DecimalMin(value = "0.00", message = "评分不能小于 0")
    @DecimalMax(value = "100.00", message = "评分不能大于 100")
    private BigDecimal score;

    private String commentText;

    @Valid
    private List<EvaluationScoreItemRequest> scoreItems = new ArrayList<>();

    public Long getMaterialVersionId() {
        return materialVersionId;
    }

    public void setMaterialVersionId(Long materialVersionId) {
        this.materialVersionId = materialVersionId;
    }

    public BigDecimal getScore() {
        return score;
    }

    public void setScore(BigDecimal score) {
        this.score = score;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public List<EvaluationScoreItemRequest> getScoreItems() {
        return scoreItems;
    }

    public void setScoreItems(List<EvaluationScoreItemRequest> scoreItems) {
        this.scoreItems = scoreItems;
    }
}
