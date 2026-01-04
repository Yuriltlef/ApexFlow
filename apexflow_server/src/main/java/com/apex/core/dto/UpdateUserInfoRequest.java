package com.apex.core.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 更新用户信息请求DTO
 * * @JsonIgnoreProperties(ignoreUnknown = true):
 * 关键注解！告诉后端：如果前端传了我没定义的字段（比如 id, username），直接忽略，不要报错。
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateUserInfoRequest {
    private String realName;
    private String email;
    private String phone;
    private Integer status;
    private String role; // [新增] 添加角色字段，否则无法修改用户角色
    private String password; // [新增] 如果允许修改密码，也可以加上（视你业务逻辑而定）

    // Getters and Setters
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}