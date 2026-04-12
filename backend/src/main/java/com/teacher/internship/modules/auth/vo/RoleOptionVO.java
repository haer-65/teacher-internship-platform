package com.teacher.internship.modules.auth.vo;

public class RoleOptionVO {

    private String roleCode;
    private String roleName;

    public RoleOptionVO() {
    }

    public RoleOptionVO(String roleCode, String roleName) {
        this.roleCode = roleCode;
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}

