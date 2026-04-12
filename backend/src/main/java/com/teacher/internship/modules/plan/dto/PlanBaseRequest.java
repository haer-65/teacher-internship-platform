package com.teacher.internship.modules.plan.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class PlanBaseRequest {

    @NotNull(message = "实习基地标识不能为空")
    private Long baseId;

    @NotNull(message = "基地名额不能为空")
    @Min(value = 1, message = "基地名额必须大于 0")
    private Integer baseQuota;

    @NotNull(message = "排序号不能为空")
    @Min(value = 1, message = "排序号必须大于 0")
    private Integer sortNo;

    private String status;

    @Size(max = 255, message = "备注长度不能超过255个字符")
    private String remark;

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public Integer getBaseQuota() {
        return baseQuota;
    }

    public void setBaseQuota(Integer baseQuota) {
        this.baseQuota = baseQuota;
    }

    public Integer getSortNo() {
        return sortNo;
    }

    public void setSortNo(Integer sortNo) {
        this.sortNo = sortNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
