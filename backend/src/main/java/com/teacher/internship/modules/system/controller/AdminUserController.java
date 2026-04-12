package com.teacher.internship.modules.system.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.system.dto.CreateUserRequest;
import com.teacher.internship.modules.system.dto.ResetPasswordRequest;
import com.teacher.internship.modules.system.dto.UpdateUserRolesRequest;
import com.teacher.internship.modules.system.dto.UpdateUserStatusRequest;
import com.teacher.internship.modules.system.service.SystemAuditService;
import com.teacher.internship.modules.system.service.UserService;
import com.teacher.internship.modules.system.vo.IdNameOptionVO;
import com.teacher.internship.modules.system.vo.UserImportResultVO;
import com.teacher.internship.modules.system.vo.UserPageVO;
import java.util.List;
import com.teacher.internship.security.auth.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
public class AdminUserController {

    private final UserService userService;
    private final SystemAuditService auditService;

    public AdminUserController(UserService userService,
                               SystemAuditService auditService) {
        this.userService = userService;
        this.auditService = auditService;
    }

    @GetMapping("/list")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<UserPageVO> list(@RequestParam(defaultValue = "1") long page,
                                        @RequestParam(defaultValue = "10") long size,
                                        @RequestParam(required = false) String keyword,
                                        @RequestParam(required = false) String status) {
        return ApiResponse.success(userService.queryUsers(page, size, keyword, status));
    }

    @GetMapping("/departments")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<List<IdNameOptionVO>> departments() {
        return ApiResponse.success(userService.queryEnabledDepartmentOptions());
    }

    @GetMapping("/majors")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<List<IdNameOptionVO>> majors(@RequestParam(required = false) Long deptId) {
        return ApiResponse.success(userService.queryEnabledMajorOptions(deptId));
    }

    @GetMapping("/grades")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<List<IdNameOptionVO>> grades() {
        return ApiResponse.success(userService.queryEnabledGradeOptions());
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long userId,
                                          @Valid @RequestBody UpdateUserStatusRequest request,
                                          HttpServletRequest httpRequest) {
        Long operatorId = SecurityUtils.currentUserId();
        if (operatorId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        userService.updateUserStatus(operatorId, userId, request.getStatus());
        auditService.logSuccess(operatorId, "USER", "STATUS_UPDATE", "SYS_USER", userId,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, null);
        return ApiResponse.success("用户状态更新成功", null);
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<Void> updateRoles(@PathVariable("id") Long userId,
                                         @Valid @RequestBody UpdateUserRolesRequest request,
                                         HttpServletRequest httpRequest) {
        Long operatorId = SecurityUtils.currentUserId();
        if (operatorId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        userService.updateUserRoles(operatorId, userId, request.getRoleCodes());
        auditService.logSuccess(operatorId, "USER", "ROLE_UPDATE", "SYS_USER", userId,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, null);
        return ApiResponse.success("用户角色更新成功", null);
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<Void> resetPassword(@PathVariable("id") Long userId,
                                           @Valid @RequestBody(required = false) ResetPasswordRequest request,
                                           HttpServletRequest httpRequest) {
        Long operatorId = SecurityUtils.currentUserId();
        if (operatorId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        userService.resetPassword(operatorId, userId, request);
        auditService.logSuccess(operatorId, "USER", "RESET_PASSWORD", "SYS_USER", userId,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), null, null);
        return ApiResponse.success("密码已重置为默认初始密码", null);
    }

    @PostMapping("/import")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<UserImportResultVO> importUsers(@RequestParam("file") MultipartFile file,
                                                       HttpServletRequest httpRequest) {
        Long operatorId = SecurityUtils.currentUserId();
        if (operatorId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        UserImportResultVO result = userService.importUsers(file, operatorId);
        auditService.logSuccess(operatorId, "USER", "IMPORT", "SYS_USER", null,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), null, result);
        return ApiResponse.success("用户导入完成", result);
    }

    @PostMapping("/create")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<Long> createUser(@Valid @RequestBody CreateUserRequest request,
                                        HttpServletRequest httpRequest) {
        Long operatorId = SecurityUtils.currentUserId();
        if (operatorId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        Long userId = userService.createUser(operatorId, request);
        auditService.logSuccess(operatorId, "USER", "CREATE", "SYS_USER", userId,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), request, null);
        return ApiResponse.success("用户创建成功", userId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<Void> deleteUser(@PathVariable("id") Long userId,
                                        HttpServletRequest httpRequest) {
        Long operatorId = SecurityUtils.currentUserId();
        if (operatorId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        userService.deleteUser(operatorId, userId);
        auditService.logSuccess(operatorId, "USER", "DELETE", "SYS_USER", userId,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), null, null);
        return ApiResponse.success("用户删除成功", null);
    }

    @PostMapping("/batch-delete")
    @PreAuthorize("@permissionService.hasPermission('user:manage')")
    public ApiResponse<Integer> batchDelete(@Valid @RequestBody List<Long> userIds,
                                            HttpServletRequest httpRequest) {
        Long operatorId = SecurityUtils.currentUserId();
        if (operatorId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        int deletedCount = userService.deleteUsers(operatorId, userIds);
        auditService.logSuccess(operatorId, "USER", "BATCH_DELETE", "SYS_USER", null,
                httpRequest.getMethod(), httpRequest.getRequestURI(), extractIp(httpRequest), userIds, deletedCount);
        return ApiResponse.success("鐢ㄦ埛鎵归噺鍒犻櫎鎴愬姛", deletedCount);
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
