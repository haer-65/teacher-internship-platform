package com.teacher.internship.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_role_menu")
@EqualsAndHashCode(callSuper = true)
public class SysRoleMenu extends BaseEntity {

    private Long roleId;
    private Long menuId;
}

