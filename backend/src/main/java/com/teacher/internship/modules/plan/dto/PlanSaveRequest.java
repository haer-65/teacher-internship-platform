package com.teacher.internship.modules.plan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlanSaveRequest {

    private String planCode;

    @NotBlank(message = "计划名称不能为空")
    private String planName;

    @NotBlank(message = "学年不能为空")
    private String academicYear;

    @NotBlank(message = "学期不能为空")
    private String term;

    @NotNull(message = "院系标识不能为空")
    private Long deptId;

    @NotNull(message = "开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotNull(message = "报名截止时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyDeadline;

    @NotNull(message = "学生名额不能为空")
    @Min(value = 1, message = "学生名额必须大于 0")
    private Integer studentQuota;

    private String description;

    @NotNull(message = "校内导师权重不能为空")
    @DecimalMin(value = "0.00", message = "校内导师权重不能小于 0")
    private BigDecimal innerTeacherWeight;

    @NotNull(message = "基地导师权重不能为空")
    @DecimalMin(value = "0.00", message = "基地导师权重不能小于 0")
    private BigDecimal baseTeacherWeight;

    @NotEmpty(message = "材料类型不能为空")
    private List<@Valid PlanMaterialTypeRequest> materialTypes = new ArrayList<>();

    @NotEmpty(message = "实习基地配置不能为空")
    private List<@Valid PlanBaseRequest> planBases = new ArrayList<>();

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

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getApplyDeadline() {
        return applyDeadline;
    }

    public void setApplyDeadline(LocalDateTime applyDeadline) {
        this.applyDeadline = applyDeadline;
    }

    public Integer getStudentQuota() {
        return studentQuota;
    }

    public void setStudentQuota(Integer studentQuota) {
        this.studentQuota = studentQuota;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getInnerTeacherWeight() {
        return innerTeacherWeight;
    }

    public void setInnerTeacherWeight(BigDecimal innerTeacherWeight) {
        this.innerTeacherWeight = innerTeacherWeight;
    }

    public BigDecimal getBaseTeacherWeight() {
        return baseTeacherWeight;
    }

    public void setBaseTeacherWeight(BigDecimal baseTeacherWeight) {
        this.baseTeacherWeight = baseTeacherWeight;
    }

    public List<PlanMaterialTypeRequest> getMaterialTypes() {
        return materialTypes;
    }

    public void setMaterialTypes(List<PlanMaterialTypeRequest> materialTypes) {
        this.materialTypes = materialTypes;
    }

    public List<PlanBaseRequest> getPlanBases() {
        return planBases;
    }

    public void setPlanBases(List<PlanBaseRequest> planBases) {
        this.planBases = planBases;
    }
}
