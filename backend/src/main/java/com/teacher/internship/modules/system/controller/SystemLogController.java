package com.teacher.internship.modules.system.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.modules.system.service.SystemLogService;
import com.teacher.internship.modules.system.vo.PageResultVO;
import com.teacher.internship.modules.system.vo.SysLoginLogItemVO;
import com.teacher.internship.modules.system.vo.SysOperationLogItemVO;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@Validated
@RestController
@RequestMapping("/api/v1/admin/system/log")
public class SystemLogController {

    private final SystemLogService logService;

    public SystemLogController(SystemLogService logService) {
        this.logService = logService;
    }

    @GetMapping("/operation/page")
    @PreAuthorize("@permissionService.hasPermission('system:view')")
    public ApiResponse<PageResultVO<SysOperationLogItemVO>> operationPage(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String moduleCode,
            @RequestParam(required = false) String actionCode,
            @RequestParam(required = false) String operationStatus,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return ApiResponse.success(logService.queryOperationPage(
                page,
                size,
                keyword,
                moduleCode,
                actionCode,
                operationStatus,
                startTime,
                endTime
        ));
    }

    @GetMapping("/login/page")
    @PreAuthorize("@permissionService.hasPermission('system:view')")
    public ApiResponse<PageResultVO<SysLoginLogItemVO>> loginPage(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String loginResult,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return ApiResponse.success(logService.queryLoginPage(
                page,
                size,
                keyword,
                loginResult,
                startTime,
                endTime
        ));
    }
}

