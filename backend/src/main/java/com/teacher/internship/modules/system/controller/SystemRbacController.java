package com.teacher.internship.modules.system.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.system.dto.RbacRoleMenuAssignRequest;
import com.teacher.internship.modules.system.service.SystemAuditService;
import com.teacher.internship.modules.system.service.SystemRbacAdminService;
import com.teacher.internship.modules.system.vo.SysMenuNodeVO;
import com.teacher.internship.modules.system.vo.SysRoleItemVO;
import com.teacher.internship.security.auth.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/admin/system/rbac")
public class SystemRbacController {

    private final SystemRbacAdminService rbacAdminService;
    private final SystemAuditService auditService;

    public SystemRbacController(SystemRbacAdminService rbacAdminService,
                                SystemAuditService auditService) {
        this.rbacAdminService = rbacAdminService;
        this.auditService = auditService;
    }

    @GetMapping("/role/list")
    @PreAuthorize("@permissionService.hasPermission('role:manage')")
    public ApiResponse<List<SysRoleItemVO>> roleList() {
        return ApiResponse.success(rbacAdminService.queryRoleList());
    }

    @GetMapping("/menu/tree")
    @PreAuthorize("@permissionService.hasPermission('role:manage')")
    public ApiResponse<List<SysMenuNodeVO>> menuTree(@RequestParam(required = false) Long roleId) {
        return ApiResponse.success(rbacAdminService.queryMenuTree(roleId));
    }

    @GetMapping("/role/menu/{roleId}")
    @PreAuthorize("@permissionService.hasPermission('role:manage')")
    public ApiResponse<List<Long>> roleMenuIds(@PathVariable("roleId") Long roleId) {
        return ApiResponse.success(rbacAdminService.queryRoleMenuIds(roleId));
    }

    @PutMapping("/role/menu/{roleId}")
    @PreAuthorize("@permissionService.hasPermission('role:manage')")
    public ApiResponse<List<Long>> assignRoleMenus(@PathVariable("roleId") Long roleId,
                                                   @Valid @RequestBody RbacRoleMenuAssignRequest request,
                                                   HttpServletRequest httpRequest) {
        Long operatorId = requireOperatorId();
        List<Long> data = rbacAdminService.assignRoleMenus(roleId, request.getMenuIds(), operatorId);
        auditService.logSuccess(operatorId, "RBAC", "ROLE_MENU_ASSIGN", "SYS_ROLE", roleId,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, data);
        return ApiResponse.success("角色菜单权限更新成功", data);
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
