package com.teacher.internship.modules.application.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import java.time.LocalDateTime;

public class AssignmentItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;
    private String planCode;
    private String planName;
    private String planStatus;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long applicationId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String studentDeptName;
    private String studentMajorName;
    private String studentGradeName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long baseId;
    private String baseCode;
    private String baseName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long innerTeacherId;
    private String innerTeacherNo;
    private String innerTeacherName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long baseTeacherId;
    private String baseTeacherNo;
    private String baseTeacherName;
    private Integer versionNo;
    private Integer isCurrent;
    private String assignmentStatus;
    private String adjustReason;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long assignedBy;
    private String assignedByName;
    private LocalDateTime assignedTime;

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

    public String getPlanStatus() {
        return planStatus;
    }

    public void setPlanStatus(String planStatus) {
        this.planStatus = planStatus;
    }

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
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

    public String getStudentDeptName() {
        return studentDeptName;
    }

    public void setStudentDeptName(String studentDeptName) {
        this.studentDeptName = studentDeptName;
    }

    public String getStudentMajorName() {
        return studentMajorName;
    }

    public void setStudentMajorName(String studentMajorName) {
        this.studentMajorName = studentMajorName;
    }

    public String getStudentGradeName() {
        return studentGradeName;
    }

    public void setStudentGradeName(String studentGradeName) {
        this.studentGradeName = studentGradeName;
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

    public Long getInnerTeacherId() {
        return innerTeacherId;
    }

    public void setInnerTeacherId(Long innerTeacherId) {
        this.innerTeacherId = innerTeacherId;
    }

    public String getInnerTeacherNo() {
        return innerTeacherNo;
    }

    public void setInnerTeacherNo(String innerTeacherNo) {
        this.innerTeacherNo = innerTeacherNo;
    }

    public String getInnerTeacherName() {
        return innerTeacherName;
    }

    public void setInnerTeacherName(String innerTeacherName) {
        this.innerTeacherName = innerTeacherName;
    }

    public Long getBaseTeacherId() {
        return baseTeacherId;
    }

    public void setBaseTeacherId(Long baseTeacherId) {
        this.baseTeacherId = baseTeacherId;
    }

    public String getBaseTeacherNo() {
        return baseTeacherNo;
    }

    public void setBaseTeacherNo(String baseTeacherNo) {
        this.baseTeacherNo = baseTeacherNo;
    }

    public String getBaseTeacherName() {
        return baseTeacherName;
    }

    public void setBaseTeacherName(String baseTeacherName) {
        this.baseTeacherName = baseTeacherName;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(Integer versionNo) {
        this.versionNo = versionNo;
    }

    public Integer getIsCurrent() {
        return isCurrent;
    }

    public void setIsCurrent(Integer isCurrent) {
        this.isCurrent = isCurrent;
    }

    public String getAssignmentStatus() {
        return assignmentStatus;
    }

    public void setAssignmentStatus(String assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }

    public String getAdjustReason() {
        return adjustReason;
    }

    public void setAdjustReason(String adjustReason) {
        this.adjustReason = adjustReason;
    }

    public Long getAssignedBy() {
        return assignedBy;
    }

    public void setAssignedBy(Long assignedBy) {
        this.assignedBy = assignedBy;
    }

    public String getAssignedByName() {
        return assignedByName;
    }

    public void setAssignedByName(String assignedByName) {
        this.assignedByName = assignedByName;
    }

    public LocalDateTime getAssignedTime() {
        return assignedTime;
    }

    public void setAssignedTime(LocalDateTime assignedTime) {
        this.assignedTime = assignedTime;
    }
}
