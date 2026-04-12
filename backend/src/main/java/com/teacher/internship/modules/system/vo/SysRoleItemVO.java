package com.teacher.internship.modules.system.vo;

import lombok.Data;

@Data
public class SysRoleItemVO {

    private Long id;
    private String roleCode;
    private String roleName;
    private String dataScope;
    private String status;
    private String remark;
}

