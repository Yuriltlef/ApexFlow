CREATE TABLE apexflow_system_user (
    id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
    password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
    salt VARCHAR(32) NOT NULL COMMENT '密码盐值（32位随机字符串）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    
    -- 7个权限字段
    is_admin BOOLEAN DEFAULT FALSE COMMENT '超级管理员（拥有所有权限）',
    can_manage_order BOOLEAN DEFAULT FALSE COMMENT '订单管理权限',
    can_manage_logistics BOOLEAN DEFAULT FALSE COMMENT '物流管理权限',
    can_manage_after_sales BOOLEAN DEFAULT FALSE COMMENT '售后管理权限',
    can_manage_review BOOLEAN DEFAULT FALSE COMMENT '评价管理权限',
    can_manage_inventory BOOLEAN DEFAULT FALSE COMMENT '库存管理权限',
    can_manage_income BOOLEAN DEFAULT FALSE COMMENT '收入管理权限',
    
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    last_login_at DATETIME COMMENT '最后登录时间'
) COMMENT='系统用户表（包含权限和盐值）';
