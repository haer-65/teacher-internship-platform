package com.teacher.internship.modules.system.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SysLoginLogItemVO {

    private Long id;
    private String accountNo;
    private Long userId;
    private String loginIp;
    private String userAgent;
    private String loginResult;
    private String failReason;
    private LocalDateTime loginTime;
}
