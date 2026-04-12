package com.teacher.internship.modules.application.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StudentAvailablePlanVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applicationId;
    private String planCode;
    private String planName;
    private String academicYear;
    private String term;
    private LocalDateTime applyDeadline;
    private String applicationStatus;
    private List<com.teacher.internship.modules.plan.vo.PlanBaseVO> planBases = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
    }

    public String getPlanCode() {
        return planCode;
    }

    public void setPlanCode(String planCode) {
        this.planCode = planCode;
    }

    public String getPlanName() {
        return planName;
    }

    public void setPlanName(String planName) {
        this.planName = planName;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public LocalDateTime getApplyDeadline() {
        return applyDeadline;
    }

    public void setApplyDeadline(LocalDateTime applyDeadline) {
        this.applyDeadline = applyDeadline;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public List<com.teacher.internship.modules.plan.vo.PlanBaseVO> getPlanBases() {
        return planBases;
    }

    public void setPlanBases(List<com.teacher.internship.modules.plan.vo.PlanBaseVO> planBases) {
        this.planBases = planBases;
    }
}
