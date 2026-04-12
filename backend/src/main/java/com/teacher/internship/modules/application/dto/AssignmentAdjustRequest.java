package com.teacher.internship.modules.application.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class AssignmentAdjustRequest {

    @NotNull(message = "实习基地不能为空")
    private Long baseId;

    @NotNull(message = "校内指导教师不能为空")
    private Long innerTeacherId;

    @NotNull(message = "基地指导教师不能为空")
    private Long baseTeacherId;

    @NotBlank(message = "调整原因不能为空")
    private String adjustReason;

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
