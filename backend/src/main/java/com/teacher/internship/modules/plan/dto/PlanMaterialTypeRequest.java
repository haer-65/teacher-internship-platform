package com.teacher.internship.modules.plan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PlanMaterialTypeRequest {

    @NotBlank(message = "材料类型编码不能为空")
    private String typeCode;

    @NotBlank(message = "材料类型名称不能为空")
    private String typeName;

    @NotNull(message = "是否必交不能为空")
    private Integer requiredFlag;

    @NotNull(message = "截止时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime deadlineTime;

    @NotNull(message = "权重不能为空")
    @DecimalMin(value = "0.00", message = "权重不能小于 0")
    private BigDecimal weight;

    private Boolean allowResubmit;
    private Integer maxSubmitCount;
    private String status;

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getRequiredFlag() {
        return requiredFlag;
    }

    public void setRequiredFlag(Integer requiredFlag) {
        this.requiredFlag = requiredFlag;
    }

    public LocalDateTime getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(LocalDateTime deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public Boolean getAllowResubmit() {
        return allowResubmit;
    }

    public void setAllowResubmit(Boolean allowResubmit) {
        this.allowResubmit = allowResubmit;
    }

    public Integer getMaxSubmitCount() {
        return maxSubmitCount;
    }

    public void setMaxSubmitCount(Integer maxSubmitCount) {
        this.maxSubmitCount = maxSubmitCount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
