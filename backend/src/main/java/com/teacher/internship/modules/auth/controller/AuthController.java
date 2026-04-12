package com.teacher.internship.modules.auth.controller;

import com.teacher.internship.common.api.ApiResponse;
import com.teacher.internship.modules.auth.dto.LoginRequest;
import com.teacher.internship.modules.auth.dto.SwitchRoleRequest;
import com.teacher.internship.modules.auth.service.AuthService;
import com.teacher.internship.modules.auth.vo.AuthContextVO;
import com.teacher.internship.security.auth.SecurityUtils;
import com.teacher.internship.security.jwt.JwtUserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthContextVO> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return ApiResponse.success(authService.login(request, httpRequest));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout() {
        return ApiResponse.success("退出成功", null);
    }

    @GetMapping("/me")
    @PreAuthorize("@permissionService.hasPermission('auth:profile')")
    public ApiResponse<AuthContextVO> me() {
        JwtUserPrincipal principal = SecurityUtils.currentPrincipal();
        return ApiResponse.success(authService.me(principal));
    }

    @PostMapping("/switch-role")
    @PreAuthorize("@permissionService.hasPermission('auth:profile')")
    public ApiResponse<AuthContextVO> switchRole(@Valid @RequestBody SwitchRoleRequest request) {
        JwtUserPrincipal principal = SecurityUtils.currentPrincipal();
        return ApiResponse.success(authService.switchRole(principal, request.getRoleCode()));
    }
}
