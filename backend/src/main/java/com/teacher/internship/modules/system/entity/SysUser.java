package com.teacher.internship.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_user")
@EqualsAndHashCode(callSuper = true)
public class SysUser extends BaseEntity {

    private String loginName;
    private String passwordHash;
    private String realName;
    private String identityType;
    private String studentNo;
    private String teacherNo;
    private String phone;
    private String email;
    private Long deptId;
    private Long majorId;
    private Long gradeId;
    private String status;
    private Integer mustChangePassword;
    private LocalDateTime lastLoginTime;
}

