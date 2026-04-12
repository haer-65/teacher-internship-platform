package com.teacher.internship.modules.application.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AdminApplicationItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;
    private String planCode;
    private String planName;
    private String planStatus;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;
    private String studentNo;
    private String studentName;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentDeptId;
    private String studentDeptName;
    private String studentMajorName;
    private String studentGradeName;
    private String personalStatement;
    private List<ApplicationPreferenceVO> preferredBases = new ArrayList<>();
    private String applicationStatus;
    private String reviewComment;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long reviewedBy;
    private String reviewedByName;
    private LocalDateTime reviewedTime;
    private LocalDateTime submittedTime;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long currentAssignmentId;
    private Integer currentAssignmentVersionNo;

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

    public Long getStudentDeptId() {
        return studentDeptId;
    }

    public void setStudentDeptId(Long studentDeptId) {
        this.studentDeptId = studentDeptId;
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

    public String getPersonalStatement() {
        return personalStatement;
    }

    public void setPersonalStatement(String personalStatement) {
        this.personalStatement = personalStatement;
    }

    public List<ApplicationPreferenceVO> getPreferredBases() {
        return preferredBases;
    }

    public void setPreferredBases(List<ApplicationPreferenceVO> preferredBases) {
        this.preferredBases = preferredBases;
    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public String getReviewComment() {
        return reviewComment;
    }

    public void setReviewComment(String reviewComment) {
        this.reviewComment = reviewComment;
    }

    public Long getReviewedBy() {
        return reviewedBy;
    }

    public void setReviewedBy(Long reviewedBy) {
        this.reviewedBy = reviewedBy;
    }

    public String getReviewedByName() {
        return reviewedByName;
    }

    public void setReviewedByName(String reviewedByName) {
        this.reviewedByName = reviewedByName;
    }

    public LocalDateTime getReviewedTime() {
        return reviewedTime;
    }

    public void setReviewedTime(LocalDateTime reviewedTime) {
        this.reviewedTime = reviewedTime;
    }

    public LocalDateTime getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(LocalDateTime submittedTime) {
        this.submittedTime = submittedTime;
    }

    public Long getCurrentAssignmentId() {
        return currentAssignmentId;
    }

    public void setCurrentAssignmentId(Long currentAssignmentId) {
        this.currentAssignmentId = currentAssignmentId;
    }

    public Integer getCurrentAssignmentVersionNo() {
        return currentAssignmentVersionNo;
    }

    public void setCurrentAssignmentVersionNo(Integer currentAssignmentVersionNo) {
        this.currentAssignmentVersionNo = currentAssignmentVersionNo;
    }
}
