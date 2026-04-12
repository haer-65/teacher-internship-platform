package com.teacher.internship.modules.auth.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public class LoginUserInfo {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long userId;
    private String accountNo;
    private String realName;
    private String identityType;
    private String studentNo;
    private String teacherNo;
    private String phone;
    private String email;
    private Long deptId;
    private String deptName;
    private String majorName;
    private String gradeName;
    private String currentRoleCode;
    private Integer mustChangePassword;

    public LoginUserInfo() {
    }

    public LoginUserInfo(Long userId,
                         String accountNo,
                         String realName,
                         String identityType,
                         String studentNo,
                         String teacherNo,
                         String phone,
                         String email,
                         Long deptId,
                         String deptName,
                         String majorName,
                         String gradeName,
                         String currentRoleCode,
                         Integer mustChangePassword) {
        this.userId = userId;
        this.accountNo = accountNo;
        this.realName = realName;
        this.identityType = identityType;
        this.studentNo = studentNo;
        this.teacherNo = teacherNo;
        this.phone = phone;
        this.email = email;
        this.deptId = deptId;
        this.deptName = deptName;
        this.majorName = majorName;
        this.gradeName = gradeName;
        this.currentRoleCode = currentRoleCode;
        this.mustChangePassword = mustChangePassword;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getAccountNo() {
        return accountNo;
    }

    public void setAccountNo(String accountNo) {
        this.accountNo = accountNo;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdentityType() {
        return identityType;
    }

    public void setIdentityType(String identityType) {
        this.identityType = identityType;
    }

    public String getStudentNo() {
        return studentNo;
    }

    public void setStudentNo(String studentNo) {
        this.studentNo = studentNo;
    }

    public String getTeacherNo() {
        return teacherNo;
    }

    public void setTeacherNo(String teacherNo) {
        this.teacherNo = teacherNo;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getMajorName() {
        return majorName;
    }

    public void setMajorName(String majorName) {
        this.majorName = majorName;
    }

    public String getGradeName() {
        return gradeName;
    }

    public void setGradeName(String gradeName) {
        this.gradeName = gradeName;
    }

    public String getCurrentRoleCode() {
        return currentRoleCode;
    }

    public void setCurrentRoleCode(String currentRoleCode) {
        this.currentRoleCode = currentRoleCode;
    }

    public Integer getMustChangePassword() {
        return mustChangePassword;
    }

    public void setMustChangePassword(Integer mustChangePassword) {
        this.mustChangePassword = mustChangePassword;
    }
}
