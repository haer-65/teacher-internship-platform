package com.teacher.internship.security.auth;

import com.teacher.internship.modules.system.service.RbacService;
import com.teacher.internship.security.jwt.JwtUserPrincipal;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Set;

@Component("permissionService")
public class PermissionService {

    private final RbacService rbacService;

    public PermissionService(RbacService rbacService) {
        this.rbacService = rbacService;
    }

    public boolean hasPermission(String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            return true;
        }

        JwtUserPrincipal principal = SecurityUtils.currentPrincipal();
        if (principal == null || !StringUtils.hasText(principal.getRoleCode())) {
            return false;
        }

        Set<String> permissionCodes = rbacService.getPermissionCodesByRoleCode(principal.getRoleCode());
        return permissionCodes.contains(permissionCode);
    }
}

