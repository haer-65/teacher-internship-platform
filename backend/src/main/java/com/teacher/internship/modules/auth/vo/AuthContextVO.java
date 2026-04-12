package com.teacher.internship.modules.auth.vo;

import java.util.ArrayList;
import java.util.List;

public class AuthContextVO {

    private String token;
    private String tokenType;
    private Long expiresIn;
    private String currentRoleCode;
    private LoginUserInfo user;
    private List<RoleOptionVO> roles = new ArrayList<>();
    private List<MenuTreeVO> menuTree = new ArrayList<>();
    private List<String> permissionCodes = new ArrayList<>();

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public String getCurrentRoleCode() {
        return currentRoleCode;
    }

    public void setCurrentRoleCode(String currentRoleCode) {
        this.currentRoleCode = currentRoleCode;
    }

    public LoginUserInfo getUser() {
        return user;
    }

    public void setUser(LoginUserInfo user) {
        this.user = user;
    }

    public List<RoleOptionVO> getRoles() {
        return roles;
    }

    public void setRoles(List<RoleOptionVO> roles) {
        this.roles = roles;
    }

    public List<MenuTreeVO> getMenuTree() {
        return menuTree;
    }

    public void setMenuTree(List<MenuTreeVO> menuTree) {
        this.menuTree = menuTree;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }
}

