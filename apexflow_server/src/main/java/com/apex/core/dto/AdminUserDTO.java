package com.apex.core.dto;

import java.time.LocalDateTime;

/**
 * 管理员用户DTO - 用于管理员查看用户信息
 */
public class AdminUserDTO {
    private Integer id;
    private String username;
    private String realName;
    private String email;
    private String phone;
    private Boolean isAdmin;
    private Boolean canManageOrder;
    private Boolean canManageLogistics;
    private Boolean canManageAfterSales;
    private Boolean canManageReview;
    private Boolean canManageInventory;
    private Boolean canManageIncome;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastLoginAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Boolean getIsAdmin() { return isAdmin; }
    public void setIsAdmin(Boolean admin) { isAdmin = admin; }

    public Boolean getCanManageOrder() { return canManageOrder; }
    public void setCanManageOrder(Boolean canManageOrder) { this.canManageOrder = canManageOrder; }

    public Boolean getCanManageLogistics() { return canManageLogistics; }
    public void setCanManageLogistics(Boolean canManageLogistics) { this.canManageLogistics = canManageLogistics; }

    public Boolean getCanManageAfterSales() { return canManageAfterSales; }
    public void setCanManageAfterSales(Boolean canManageAfterSales) { this.canManageAfterSales = canManageAfterSales; }

    public Boolean getCanManageReview() { return canManageReview; }
    public void setCanManageReview(Boolean canManageReview) { this.canManageReview = canManageReview; }

    public Boolean getCanManageInventory() { return canManageInventory; }
    public void setCanManageInventory(Boolean canManageInventory) { this.canManageInventory = canManageInventory; }

    public Boolean getCanManageIncome() { return canManageIncome; }
    public void setCanManageIncome(Boolean canManageIncome) { this.canManageIncome = canManageIncome; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public LocalDateTime getLastLoginAt() { return lastLoginAt; }
    public void setLastLoginAt(LocalDateTime lastLoginAt) { this.lastLoginAt = lastLoginAt; }
}

