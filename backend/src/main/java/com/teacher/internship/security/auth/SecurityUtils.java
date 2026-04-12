package com.teacher.internship.security.auth;

import com.teacher.internship.security.jwt.JwtUserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static JwtUserPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof JwtUserPrincipal)) {
            return null;
        }
        return (JwtUserPrincipal) authentication.getPrincipal();
    }

    public static Long currentUserId() {
        JwtUserPrincipal principal = currentPrincipal();
        return principal == null ? null : principal.getUserId();
    }

    public static String currentRoleCode() {
        JwtUserPrincipal principal = currentPrincipal();
        return principal == null ? null : principal.getRoleCode();
    }
}

