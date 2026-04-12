package com.teacher.internship.modules.application.dto;

import javax.validation.constraints.NotBlank;

public class ApplicationReviewRequest {

    @NotBlank(message = "审核状态不能为空")
    private String status;

    private String reviewComment;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }
}
