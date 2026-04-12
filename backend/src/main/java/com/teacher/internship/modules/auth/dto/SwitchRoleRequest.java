package com.teacher.internship.modules.auth.dto;

import javax.validation.constraints.NotBlank;

public class SwitchRoleRequest {

    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }
}
