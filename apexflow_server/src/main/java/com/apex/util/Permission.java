package com.apex.util;

/**
 * 权限类型枚举
 * 包含管理员权限和业务权限
 */
public enum Permission {
    // 系统权限
    ADMIN("管理员权限"),

    // 业务权限
    ORDER_MANAGE("订单管理"),
    LOGISTICS_MANAGE("物流管理"),
    AFTER_SALES_MANAGE("售后管理"),
    REVIEW_MANAGE("评价管理"),
    INVENTORY_MANAGE("库存管理"),
    INCOME_MANAGE("收入管理"),

    // 扩展权限可以在这里继续添加
    SYSTEM_CONFIG("系统配置"),
    USER_MANAGE("用户管理"),
    ROLE_MANAGE("角色管理"),
    DATA_EXPORT("数据导出"),
    DATA_IMPORT("数据导入");

    private final String description;

    Permission(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 将枚举转换为数据库字段名
     */
    public String toDbField() {
        switch (this) {
            case ADMIN: return "is_admin";
            case ORDER_MANAGE: return "can_manage_order";
            case LOGISTICS_MANAGE: return "can_manage_logistics";
            case AFTER_SALES_MANAGE: return "can_manage_after_sales";
            case REVIEW_MANAGE: return "can_manage_review";
            case INVENTORY_MANAGE: return "can_manage_inventory";
            case INCOME_MANAGE: return "can_manage_income";
            case SYSTEM_CONFIG: return "can_system_config";
            case USER_MANAGE: return "can_manage_user";
            case ROLE_MANAGE: return "can_manage_role";
            case DATA_EXPORT: return "can_export_data";
            case DATA_IMPORT: return "can_import_data";
            default: throw new IllegalArgumentException("未知的权限类型: " + this);
        }
    }

    /**
     * 判断是否是管理员权限
     */
    public boolean isAdminPermission() {
        return this == ADMIN;
    }
}