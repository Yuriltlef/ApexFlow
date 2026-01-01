package com.apex.api.request;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 20, message = "用户名长度必须在3-20个字符之间")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 20, message = "密码长度必须在6-20个字符之间")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    @Size(min = 2, max = 20, message = "真实姓名长度必须在2-20个字符之间")
    private String realName;

    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            message = "邮箱格式不正确")
    private String email;

    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private Boolean isAdmin = false;
    private Boolean canManageOrder = false;
    private Boolean canManageLogistics = false;
    private Boolean canManageAfterSales = false;
    private Boolean canManageReview = false;
    private Boolean canManageInventory = false;
    private Boolean canManageIncome = false;

    public CreateUserRequest() {}

    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

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

    @Override
    public String toString() {
        return "CreateUserRequest{" +
                "username='" + username + '\'' +
                ", password='[PROTECTED]'" +
                ", realName='" + realName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", isAdmin=" + isAdmin +
                ", canManageOrder=" + canManageOrder +
                ", canManageLogistics=" + canManageLogistics +
                ", canManageAfterSales=" + canManageAfterSales +
                ", canManageReview=" + canManageReview +
                ", canManageInventory=" + canManageInventory +
                ", canManageIncome=" + canManageIncome +
                '}';
    }
}