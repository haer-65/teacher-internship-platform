package com.teacher.internship.modules.system.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.system.dto.SysParamSaveRequest;
import com.teacher.internship.modules.system.dto.SystemStatusUpdateRequest;
import com.teacher.internship.modules.system.service.SystemAuditService;
import com.teacher.internship.modules.system.service.SystemParamService;
import com.teacher.internship.modules.system.vo.PageResultVO;
import com.teacher.internship.modules.system.vo.SysParamItemVO;
import com.teacher.internship.security.auth.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/admin/system/param")
public class SystemParamController {

    private final SystemParamService paramService;
    private final SystemAuditService auditService;

    public SystemParamController(SystemParamService paramService,
                                 SystemAuditService auditService) {
        this.paramService = paramService;
        this.auditService = auditService;
    }

    @GetMapping("/page")
    @PreAuthorize("@permissionService.hasPermission('param:manage')")
    public ApiResponse<PageResultVO<SysParamItemVO>> page(@RequestParam(defaultValue = "1") long page,
                                                          @RequestParam(defaultValue = "10") long size,
                                                          @RequestParam(required = false) String keyword,
                                                          @RequestParam(required = false) String paramType,
                                                          @RequestParam(required = false) String status) {
        return ApiResponse.success(paramService.queryPage(page, size, keyword, paramType, status));
    }

    @GetMapping("/detail/{id}")
    @PreAuthorize("@permissionService.hasPermission('param:manage')")
    public ApiResponse<SysParamItemVO> detail(@PathVariable("id") Long id) {
        return ApiResponse.success(paramService.getById(id));
    }

    @PostMapping("/create")
    @PreAuthorize("@permissionService.hasPermission('param:manage')")
    public ApiResponse<SysParamItemVO> create(@Valid @RequestBody SysParamSaveRequest request,
                                              HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        SysParamItemVO data = paramService.create(operatorId, request);
        auditService.logSuccess(operatorId, "PARAM", "CREATE", "SYS_PARAM", data.getId(),
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("参数新增成功", data);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("@permissionService.hasPermission('param:manage')")
    public ApiResponse<SysParamItemVO> update(@PathVariable("id") Long id,
                                              @Valid @RequestBody SysParamSaveRequest request,
                                              HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        SysParamItemVO data = paramService.update(operatorId, id, request);
        auditService.logSuccess(operatorId, "PARAM", "UPDATE", "SYS_PARAM", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("参数更新成功", data);
    }

    @PutMapping("/status/{id}")
    @PreAuthorize("@permissionService.hasPermission('param:manage')")
    public ApiResponse<SysParamItemVO> updateStatus(@PathVariable("id") Long id,
                                                    @Valid @RequestBody SystemStatusUpdateRequest request,
                                                    HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        SysParamItemVO data = paramService.updateStatus(operatorId, id, request.getStatus());
        auditService.logSuccess(operatorId, "PARAM", "STATUS", "SYS_PARAM", id,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("参数状态更新成功", data);
    }

    private Long requireOperatorId() {
        Long userId = SecurityUtils.currentUserId();
        if (userId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        return userId;
    }

    private String extractIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.trim().isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
