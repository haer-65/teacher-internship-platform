package com.teacher.internship.modules.plan.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PlanDetailVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    private String planCode;
    private String planName;
    private String academicYear;
    private String term;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long deptId;
    private String deptName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime applyDeadline;
    private Integer studentQuota;
    private String description;
    private BigDecimal innerTeacherWeight;
    private BigDecimal baseTeacherWeight;
    private String planStatus;
    private String scorePublishStatus;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishedTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime archivedTime;
    private List<PlanBaseVO> planBases = new ArrayList<>();
    private List<PlanMaterialTypeVO> materialTypes = new ArrayList<>();
    private List<PlanAttachmentVO> attachments = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
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

    public String getPlanStatus() {
        return planStatus;
    }

    public void setPlanStatus(String planStatus) {
        this.planStatus = planStatus;
    }

    public String getScorePublishStatus() {
        return scorePublishStatus;
    }

    public void setScorePublishStatus(String scorePublishStatus) {
        this.scorePublishStatus = scorePublishStatus;
    }

    public LocalDateTime getPublishedTime() {
        return publishedTime;
    }

    public void setPublishedTime(LocalDateTime publishedTime) {
        this.publishedTime = publishedTime;
    }

    public LocalDateTime getArchivedTime() {
        return archivedTime;
    }

    public void setArchivedTime(LocalDateTime archivedTime) {
        this.archivedTime = archivedTime;
    }

    public List<PlanBaseVO> getPlanBases() {
        return planBases;
    }

    public void setPlanBases(List<PlanBaseVO> planBases) {
        this.planBases = planBases;
    }

    public List<PlanMaterialTypeVO> getMaterialTypes() {
        return materialTypes;
    }

    public void setMaterialTypes(List<PlanMaterialTypeVO> materialTypes) {
        this.materialTypes = materialTypes;
    }

    public List<PlanAttachmentVO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<PlanAttachmentVO> attachments) {
        this.attachments = attachments;
    }
}
