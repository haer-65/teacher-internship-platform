package com.teacher.internship.modules.system.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.common.enums.ApiCode;
import com.teacher.internship.common.exception.BusinessException;
import com.teacher.internship.modules.system.dto.StudentRegisterRequest;
import com.teacher.internship.modules.system.dto.UpdatePasswordRequest;
import com.teacher.internship.modules.system.dto.UpdateProfileRequest;
import com.teacher.internship.modules.system.service.UserService;
import com.teacher.internship.modules.system.vo.IdNameOptionVO;
import com.teacher.internship.security.auth.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<Void> register(@Valid @RequestBody StudentRegisterRequest request) {
        userService.registerStudent(request);
        return ApiResponse.success("注册成功，请等待管理员审核。", null);
    }

    @GetMapping("/departments")
    public ApiResponse<List<IdNameOptionVO>> departments() {
        return ApiResponse.success(userService.queryEnabledDepartmentOptions());
    }

    @GetMapping("/majors")
    public ApiResponse<List<IdNameOptionVO>> majors(@RequestParam(required = false) Long deptId) {
        return ApiResponse.success(userService.queryEnabledMajorOptions(deptId));
    }

    @GetMapping("/grades")
    public ApiResponse<List<IdNameOptionVO>> grades() {
        return ApiResponse.success(userService.queryEnabledGradeOptions());
    }

    @PutMapping("/profile")
    @PreAuthorize("@permissionService.hasPermission('auth:profile')")
    public ApiResponse<Void> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.currentUserId();
        if (userId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        userService.updateProfile(userId, request);
        return ApiResponse.success("个人信息更新成功", null);
    }

    @PutMapping("/password")
    @PreAuthorize("@permissionService.hasPermission('auth:profile')")
    public ApiResponse<Void> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        Long userId = SecurityUtils.currentUserId();
        if (userId == null) {
            throw new BusinessException(ApiCode.UNAUTHORIZED.getCode(), ApiCode.UNAUTHORIZED.getMessage());
        }
        userService.updatePassword(userId, request);
        return ApiResponse.success("密码修改成功", null);
    }
}
