package com.teacher.internship.modules.system.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public class UpdateProfileRequest {

    @NotBlank(message = "姓名不能为空")
    private String realName;

    private String phone;

    @Email(message = "邮箱格式不正确")
    private String email;

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
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
}
