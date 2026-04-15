package com.teacher.internship.modules.plan.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class PlanBaseVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long baseId;

    private String baseCode;
    private String baseName;
    private Integer baseQuota;
    private Integer remainingQuota;
    private Integer sortNo;
    private String status;
    private String remark;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Long getBaseId() {
        return baseId;
    }

    public void setBaseId(Long baseId) {
        this.baseId = baseId;
    }

    public String getBaseCode() {
        return baseCode;
    }

    public void setBaseCode(String baseCode) {
        this.baseCode = baseCode;
    }

    public String getBaseName() {
        return baseName;
    }

    public void setBaseName(String baseName) {
        this.baseName = baseName;
    }

    public Integer getBaseQuota() {
        return baseQuota;
    }

    public void setBaseQuota(Integer baseQuota) {
        this.baseQuota = baseQuota;
    }

    public Integer getRemainingQuota() {
        return remainingQuota;
    }

    public void setRemainingQuota(Integer remainingQuota) {
        this.remainingQuota = remainingQuota;
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
