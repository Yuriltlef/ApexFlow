package com.apex.core.dto;

/**
 * 更新用户信息请求DTO
 */
public class UpdateUserInfoRequest {
    private String realName;
    private String email;
    private String phone;
    private Integer status;

    // Getters and Setters
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
