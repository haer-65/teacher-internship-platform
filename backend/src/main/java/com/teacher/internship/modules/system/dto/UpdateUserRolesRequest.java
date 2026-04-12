package com.teacher.internship.modules.system.dto;

import javax.validation.constraints.NotEmpty;
import java.util.List;

public class UpdateUserRolesRequest {

    @NotEmpty(message = "角色不能为空")
    private List<String> roleCodes;

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    public void setRoleCodes(List<String> roleCodes) {
        this.roleCodes = roleCodes;
    }
}
