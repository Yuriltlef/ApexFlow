package com.apex.core.dto;

/**
 * 重置密码请求DTO
 */
public class ResetPasswordRequest {
    private String newPassword;

    // Getters and Setters
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
