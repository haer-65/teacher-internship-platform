package com.teacher.internship.modules.material.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;

public class MaterialListItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long materialId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assignmentId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;
    private String planCode;
    private String planName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;
    private String studentNo;
    private String studentName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long materialTypeId;
    private String materialTypeCode;
    private String materialTypeName;
    private Integer requiredFlag;
    private Integer maxSubmitCount;
    private LocalDateTime deadlineTime;
    private Integer latestVersionNo;
    private String materialStatus;
    private LocalDateTime lastSubmitTime;
    private Boolean overdue;
    private Boolean canSubmit;
    private Boolean lateSubmitOpen;
    private LocalDateTime lateSubmitUntil;
    private String lateSubmitReason;
    private MaterialVersionVO latestVersion;

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public Long getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Long assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
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

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public Long getMaterialTypeId() {
        return materialTypeId;
    }

    public void setMaterialTypeId(Long materialTypeId) {
        this.materialTypeId = materialTypeId;
    }

    public String getMaterialTypeCode() {
        return materialTypeCode;
    }

    public void setMaterialTypeCode(String materialTypeCode) {
        this.materialTypeCode = materialTypeCode;
    }

    public String getMaterialTypeName() {
        return materialTypeName;
    }

    public void setMaterialTypeName(String materialTypeName) {
        this.materialTypeName = materialTypeName;
    }

    public Integer getRequiredFlag() {
        return requiredFlag;
    }

    public void setRequiredFlag(Integer requiredFlag) {
        this.requiredFlag = requiredFlag;
    }

    public Integer getMaxSubmitCount() {
        return maxSubmitCount;
    }

    public void setMaxSubmitCount(Integer maxSubmitCount) {
        this.maxSubmitCount = maxSubmitCount;
    }

    public LocalDateTime getDeadlineTime() {
        return deadlineTime;
    }

    public void setDeadlineTime(LocalDateTime deadlineTime) {
        this.deadlineTime = deadlineTime;
    }

    public Integer getLatestVersionNo() {
        return latestVersionNo;
    }

    public void setLatestVersionNo(Integer latestVersionNo) {
        this.latestVersionNo = latestVersionNo;
    }

    public String getMaterialStatus() {
        return materialStatus;
    }

    public void setMaterialStatus(String materialStatus) {
        this.materialStatus = materialStatus;
    }

    public LocalDateTime getLastSubmitTime() {
        return lastSubmitTime;
    }

    public void setLastSubmitTime(LocalDateTime lastSubmitTime) {
        this.lastSubmitTime = lastSubmitTime;
    }

    public Boolean getOverdue() {
        return overdue;
    }

    public void setOverdue(Boolean overdue) {
        this.overdue = overdue;
    }

    public Boolean getCanSubmit() {
        return canSubmit;
    }

    public void setCanSubmit(Boolean canSubmit) {
        this.canSubmit = canSubmit;
    }

    public Boolean getLateSubmitOpen() {
        return lateSubmitOpen;
    }

    public void setLateSubmitOpen(Boolean lateSubmitOpen) {
        this.lateSubmitOpen = lateSubmitOpen;
    }

    public LocalDateTime getLateSubmitUntil() {
        return lateSubmitUntil;
    }

    public void setLateSubmitUntil(LocalDateTime lateSubmitUntil) {
        this.lateSubmitUntil = lateSubmitUntil;
    }

    public String getLateSubmitReason() {
        return lateSubmitReason;
    }

    public void setLateSubmitReason(String lateSubmitReason) {
        this.lateSubmitReason = lateSubmitReason;
    }

    public MaterialVersionVO getLatestVersion() {
        return latestVersion;
    }

    public void setLatestVersion(MaterialVersionVO latestVersion) {
        this.latestVersion = latestVersion;
    }
}
