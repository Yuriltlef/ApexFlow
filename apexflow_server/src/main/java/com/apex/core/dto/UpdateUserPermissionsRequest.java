package com.apex.core.dto;

/**
 * 更新用户权限请求DTO
 */
public class UpdateUserPermissionsRequest {
    private Boolean isAdmin;
    private Boolean canManageOrder;
    private Boolean canManageLogistics;
    private Boolean canManageAfterSales;
    private Boolean canManageReview;
    private Boolean canManageInventory;
    private Boolean canManageIncome;

    // Getters and Setters
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
}
