package com.teacher.internship.security.jwt;

import java.io.Serializable;

public class JwtUserPrincipal implements Serializable {

    private final Long userId;
    private final String loginName;
    private final String roleCode;

    public JwtUserPrincipal(Long userId, String loginName, String roleCode) {
        this.userId = userId;
        this.loginName = loginName;
        this.roleCode = roleCode;
    }

    public Long getUserId() {
        return userId;
    }

    public String getLoginName() {
        return loginName;
    }

    public String getRoleCode() {
        return roleCode;
    }
}

