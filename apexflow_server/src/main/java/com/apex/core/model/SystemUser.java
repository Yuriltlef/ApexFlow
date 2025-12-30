package com.apex.core.model;

import java.time.LocalDateTime;

/**
 * Represents a user in the system, encapsulating their personal information, roles, and permissions.
 * This class is designed to handle user data including contact details, administrative rights,
 * specific management capabilities, and status within the system. It also tracks creation, update,
 * and last login times for each user.
 */
public class SystemUser {
    /**
     * Represents the unique identifier for a SystemUser. This field is used to uniquely
     * identify each user within the system and is essential for operations that require
     * referencing or managing specific user records.
     */
    private Integer id;
    /**
     * Represents the username of the system user. This is a unique identifier used for logging into the system.
     */
    private String username;
    /**
     * Represents the hashed value of a user's password.
     * This field is used to securely store a user's password after it has been
     * processed through a hashing algorithm, typically with a salt for added security.
     * The actual password is never stored in plain text, enhancing the security of the system.
     */
    private String passwordHash;
    /**
     * A cryptographic salt used in conjunction with the password to enhance security.
     * This value is combined with the user's password before hashing, making it more
     * difficult for attackers to use precomputed hash attacks (such as rainbow tables).
     */
    private String salt;
    /**
     * Represents the actual name of the system user, which is typically the full name or a preferred name used for identification beyond the username.
     */
    private String realName;
    /**
     * Represents the email address of the system user. This field is used for communication and identification purposes.
     */
    private String email;
    /**
     * Represents the phone number of the SystemUser. This field is used to store
     * the contact phone number for communication purposes.
     */
    private String phone;

    /**
     * Indicates whether the user has administrative privileges.
     * A value of true signifies that the user is an administrator, while false indicates a regular user.
     */
    private Boolean isAdmin;
    /**
     * Indicates whether the user has the permission to manage orders.
     * A value of true means the user is allowed to perform order management tasks,
     * while false indicates that the user does not have this permission.
     */
    private Boolean canManageOrder;
    /**
     * Indicates whether the user has the permission to manage logistics.
     * If set to true, the user can perform actions related to logistics management.
     */
    private Boolean canManageLogistics;
    /**
     * Indicates whether the user has permission to manage after-sales services.
     * A value of true means the user can access and modify after-sales related functionalities,
     * while false indicates that the user does not have this permission.
     */
    private Boolean canManageAfterSales;
    /**
     * Indicates whether the user has the permission to manage reviews.
     * A value of true means the user is allowed to perform actions related to review management,
     * such as approving, editing, or deleting reviews. A value of false denies the user these capabilities.
     */
    private Boolean canManageReview;
    /**
     * Indicates whether the user has the permission to manage inventory.
     * If true, the user is allowed to perform operations related to inventory management.
     * If false, the user does not have the necessary permissions for inventory management.
     */
    private Boolean canManageInventory;
    /**
     * Indicates whether the user has permission to manage income-related operations.
     * A value of true signifies that the user is authorized to perform such actions,
     * while false indicates that the user does not have this permission.
     */
    private Boolean canManageIncome;

    /**
     * Represents the status of the system user, where 1 indicates the user is active and 0 indicates the user is disabled.
     */
    private Integer status; // 1-正常，0-禁用
    /**
     * Represents the date and time when the SystemUser was created.
     * This field is automatically set to the current date and time upon the creation of a new SystemUser instance.
     */
    private LocalDateTime createdAt;
    /**
     * Represents the date and time when the SystemUser record was last updated.
     * This field is automatically set to the current date and time whenever
     * an update operation is performed on the SystemUser entity.
     */
    private LocalDateTime updatedAt;
    /**
     * Represents the date and time when the user last logged into the system.
     * This field is useful for tracking user activity and implementing security measures
     * such as requiring a password reset after a period of inactivity.
     */
    private LocalDateTime lastLoginAt;

    public SystemUser() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Boolean getAdmin() {
        return isAdmin;
    }

    public void setAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public Boolean getCanManageOrder() {
        return canManageOrder;
    }

    public void setCanManageOrder(Boolean canManageOrder) {
        this.canManageOrder = canManageOrder;
    }

    public Boolean getCanManageLogistics() {
        return canManageLogistics;
    }

    public void setCanManageLogistics(Boolean canManageLogistics) {
        this.canManageLogistics = canManageLogistics;
    }

    public Boolean getCanManageAfterSales() {
        return canManageAfterSales;
    }

    public void setCanManageAfterSales(Boolean canManageAfterSales) {
        this.canManageAfterSales = canManageAfterSales;
    }

    public Boolean getCanManageReview() {
        return canManageReview;
    }

    public void setCanManageReview(Boolean canManageReview) {
        this.canManageReview = canManageReview;
    }

    public Boolean getCanManageInventory() {
        return canManageInventory;
    }

    public void setCanManageInventory(Boolean canManageInventory) {
        this.canManageInventory = canManageInventory;
    }

    public Boolean getCanManageIncome() {
        return canManageIncome;
    }

    public void setCanManageIncome(Boolean canManageIncome) {
        this.canManageIncome = canManageIncome;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
