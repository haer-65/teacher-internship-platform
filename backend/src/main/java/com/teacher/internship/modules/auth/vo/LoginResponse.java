package com.teacher.internship.modules.auth.vo;

import java.util.List;

public class LoginResponse {

    private String token;
    private String tokenType;
    private long expiresIn;
    private LoginUserInfo user;
    private List<String> permissionCodes;

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

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    public LoginUserInfo getUser() {
        return user;
    }

    public void setUser(LoginUserInfo user) {
        this.user = user;
    }

    public List<String> getPermissionCodes() {
        return permissionCodes;
    }

    public void setPermissionCodes(List<String> permissionCodes) {
        this.permissionCodes = permissionCodes;
    }
}
