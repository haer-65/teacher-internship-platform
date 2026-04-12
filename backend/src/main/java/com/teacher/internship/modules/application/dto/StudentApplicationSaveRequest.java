package com.teacher.internship.modules.application.dto;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

public class StudentApplicationSaveRequest {

    @NotNull(message = "实习计划不能为空")
    private Long planId;

    @NotEmpty(message = "实习基地志愿不能为空")
    private List<Long> preferredBaseIds = new ArrayList<>();

    private String personalStatement;

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public List<Long> getPreferredBaseIds() {
        return preferredBaseIds;
    }

    public void setPreferredBaseIds(List<Long> preferredBaseIds) {
        this.preferredBaseIds = preferredBaseIds;
    }

    public String getPersonalStatement() {
        return personalStatement;
    }

    public void setPersonalStatement(String personalStatement) {
        this.personalStatement = personalStatement;
    }
}
