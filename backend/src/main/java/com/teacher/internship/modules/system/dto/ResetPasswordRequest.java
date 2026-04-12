package com.teacher.internship.modules.system.dto;

import javax.validation.constraints.Size;

public class ResetPasswordRequest {

    @Size(min = 6, max = 32, message = "新密码长度必须在6到32位之间")
    private String newPassword;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
