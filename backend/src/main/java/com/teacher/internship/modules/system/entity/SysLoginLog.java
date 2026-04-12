package com.teacher.internship.modules.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.teacher.internship.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_login_log")
@EqualsAndHashCode(callSuper = true)
public class SysLoginLog extends BaseEntity {

    private String loginName;
    private Long userId;
    private String loginIp;
    private String userAgent;
    private String loginResult;
    private String failReason;
    private LocalDateTime loginTime;
}

