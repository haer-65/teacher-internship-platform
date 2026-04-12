package com.teacher.internship.modules.application.dto;

import javax.validation.constraints.NotNull;

public class AssignmentManualRequest {

    @NotNull(message = "申请标识不能为空")
    private Long applicationId;

    @NotNull(message = "实习基地不能为空")
    private Long baseId;

    @NotNull(message = "校内指导教师不能为空")
    private Long innerTeacherId;

    @NotNull(message = "基地指导教师不能为空")
    private Long baseTeacherId;

    private String adjustReason;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public Long getInnerTeacherId() {
        return innerTeacherId;
    }

    public void setInnerTeacherId(Long innerTeacherId) {
        this.innerTeacherId = innerTeacherId;
    }

    public Long getBaseTeacherId() {
        return baseTeacherId;
    }

    public void setBaseTeacherId(Long baseTeacherId) {
        this.baseTeacherId = baseTeacherId;
    }

    public String getAdjustReason() {
        return adjustReason;
    }

    public void setAdjustReason(String adjustReason) {
        this.adjustReason = adjustReason;
    }
}
