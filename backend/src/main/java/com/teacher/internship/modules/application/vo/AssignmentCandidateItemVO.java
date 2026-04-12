package com.teacher.internship.modules.application.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AssignmentCandidateItemVO {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long applicationId;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long planId;
    private String planCode;
    private String planName;
    private String planStatus;
    @JsonSerialize(using = ToStringSerializer.class)
    private Long studentId;
    private String studentNo;
    private String studentName;
    private String studentDeptName;
    private String studentMajorName;
    private String studentGradeName;
    private List<ApplicationPreferenceVO> preferredBases = new ArrayList<>();
    private LocalDateTime submittedTime;

    public Long getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Long applicationId) {
        this.applicationId = applicationId;
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

    public List<ApplicationPreferenceVO> getPreferredBases() {
        return preferredBases;
    }

    public void setPreferredBases(List<ApplicationPreferenceVO> preferredBases) {
        this.preferredBases = preferredBases;
    }

    public LocalDateTime getSubmittedTime() {
        return submittedTime;
    }

    public void setSubmittedTime(LocalDateTime submittedTime) {
        this.submittedTime = submittedTime;
    }
}
