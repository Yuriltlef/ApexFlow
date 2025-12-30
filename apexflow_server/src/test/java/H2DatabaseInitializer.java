import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.Statement;

/**
 * H2内存数据库初始化工具类
 * 用于在单元测试中创建表结构和插入测试数据
 */
public class H2DatabaseInitializer {
    private static final Logger logger = LoggerFactory.getLogger(H2DatabaseInitializer.class);

    /**
     * 初始化H2数据库，创建所有表结构和插入测试数据
     */
    public static void initialize(Connection conn) {
        try {
            logger.info("Initializing H2 in-memory database...");

            // 禁用外键约束检查，避免创建顺序问题
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");
            }

            // 按照依赖顺序创建表
            createTables(conn);

            // 按照依赖顺序插入数据
            insertTestData(conn);

            // 重新启用外键约束检查
            try (Statement stmt = conn.createStatement()) {
                stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            }

            logger.info("H2 database initialization completed successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize H2 database", e);
            throw new RuntimeException("H2 database initialization failed", e);
        }
    }

    private static void createTables(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // 1. 创建用户表（最基础的表）
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS apexflow_system_user (
                            id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
                            username VARCHAR(50) NOT NULL UNIQUE COMMENT '登录用户名',
                            password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希值',
                            salt VARCHAR(32) NOT NULL COMMENT '密码盐值（32位随机字符串）',
                            real_name VARCHAR(50) COMMENT '真实姓名',
                            email VARCHAR(100) COMMENT '邮箱',
                            phone VARCHAR(20) COMMENT '手机号',
                            is_admin BOOLEAN DEFAULT FALSE COMMENT '超级管理员（拥有所有权限）',
                            can_manage_order BOOLEAN DEFAULT FALSE COMMENT '订单管理权限',
                            can_manage_logistics BOOLEAN DEFAULT FALSE COMMENT '物流管理权限',
                            can_manage_after_sales BOOLEAN DEFAULT FALSE COMMENT '售后管理权限',
                            can_manage_review BOOLEAN DEFAULT FALSE COMMENT '评价管理权限',
                            can_manage_inventory BOOLEAN DEFAULT FALSE COMMENT '库存管理权限',
                            can_manage_income BOOLEAN DEFAULT FALSE COMMENT '收入管理权限',
                            status TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            last_login_at TIMESTAMP COMMENT '最后登录时间'
                        )
                    """);

            // 2. 创建商品表（订单相关表依赖）
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS apexflow_product (
                            id INT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
                            name VARCHAR(100) NOT NULL COMMENT '商品名称',
                            category VARCHAR(50) COMMENT '商品分类',
                            price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
                            stock INT DEFAULT 0 COMMENT '库存数量',
                            status TINYINT DEFAULT 1 COMMENT '状态：1-上架，0-下架',
                            image VARCHAR(200) COMMENT '主图URL',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
                        )
                    """);

            // 3. 创建订单主表
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS apexflow_order (
                            id VARCHAR(50) PRIMARY KEY COMMENT '订单号，格式如ORDER20231215001',
                            user_id INT NOT NULL COMMENT '下单用户ID',
                            total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
                            status TINYINT NOT NULL COMMENT '状态：1-待支付，2-已支付，3-已发货，4-已完成，5-已取消',
                            payment_method VARCHAR(20) COMMENT '支付方式：alipay,wxpay等',
                            address_id INT COMMENT '收货地址ID',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
                            paid_at TIMESTAMP COMMENT '支付时间',
                            shipped_at TIMESTAMP COMMENT '发货时间',
                            completed_at TIMESTAMP COMMENT '完成时间'
                        )
                    """);

            // 4. 创建订单商品明细表（依赖订单和商品）
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS apexflow_order_item (
                            id INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项ID',
                            order_id VARCHAR(50) NOT NULL COMMENT '关联订单号',
                            product_id INT NOT NULL COMMENT '商品ID',
                            product_name VARCHAR(100) NOT NULL COMMENT '商品名称（下单时的快照）',
                            quantity INT NOT NULL COMMENT '购买数量',
                            price DECIMAL(10,2) NOT NULL COMMENT '下单时单价',
                            subtotal DECIMAL(10,2) NOT NULL COMMENT '小计金额',
                            FOREIGN KEY (order_id) REFERENCES apexflow_order(id),
                            FOREIGN KEY (product_id) REFERENCES apexflow_product(id)
                        )
                    """);

            // 5. 创建库存变更日志表（依赖商品）
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS apexflow_inventory_log (
                            id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
                            product_id INT NOT NULL COMMENT '关联商品ID',
                            change_type VARCHAR(20) COMMENT '变更类型：sale-销售，purchase-采购，adjust-调整',
                            quantity INT NOT NULL COMMENT '变更数量（正数增加，负数减少）',
                            before_stock INT COMMENT '变更前库存',
                            after_stock INT COMMENT '变更后库存',
                            order_id VARCHAR(50) COMMENT '关联订单号（如果是销售）',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '变更时间',
                            FOREIGN KEY (product_id) REFERENCES apexflow_product(id)
                        )
                    """);

            // 6. 创建售后服务表（依赖订单）
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS apexflow_after_sales (
                            id INT PRIMARY KEY AUTO_INCREMENT COMMENT '售后单ID',
                            order_id VARCHAR(50) NOT NULL COMMENT '关联订单号',
                            type TINYINT NOT NULL COMMENT '售后类型：1-退货，2-换货，3-维修',
                            reason VARCHAR(500) COMMENT '申请原因',
                            status TINYINT DEFAULT 1 COMMENT '状态：1-申请中，2-审核通过，3-审核拒绝，4-已完成',
                            refund_amount DECIMAL(10,2) COMMENT '退款金额',
                            apply_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
                            process_time TIMESTAMP COMMENT '处理时间',
                            process_remark VARCHAR(200) COMMENT '处理备注',
                            FOREIGN KEY (order_id) REFERENCES apexflow_order(id)
                        )
                    """);

            // 7. 创建财务收支记录表（依赖订单）
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS apexflow_income (
                            id INT PRIMARY KEY AUTO_INCREMENT COMMENT '财务记录ID',
                            order_id VARCHAR(50) NOT NULL COMMENT '关联订单号',
                            type VARCHAR(20) COMMENT '类型：income-收入，refund-退款',
                            amount DECIMAL(10,2) NOT NULL COMMENT '金额（正数为收入，负数为退款）',
                            payment_method VARCHAR(20) COMMENT '支付方式',
                            status TINYINT DEFAULT 1 COMMENT '状态：1-待入账，2-已入账',
                            transaction_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '交易时间',
                            remark VARCHAR(200) COMMENT '备注',
                            FOREIGN KEY (order_id) REFERENCES apexflow_order(id)
                        )
                    """);

            // 8. 创建物流信息表（依赖订单）
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS apexflow_logistics (
                            id INT PRIMARY KEY AUTO_INCREMENT COMMENT '物流ID',
                            order_id VARCHAR(50) NOT NULL UNIQUE COMMENT '关联订单号，一个订单一个物流',
                            express_company VARCHAR(50) COMMENT '快递公司',
                            tracking_number VARCHAR(100) COMMENT '运单号',
                            status VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending-待发货，shipped-已发货，delivered-已送达',
                            sender_address VARCHAR(200) COMMENT '发货地址',
                            receiver_address VARCHAR(200) COMMENT '收货地址',
                            shipped_at TIMESTAMP COMMENT '发货时间',
                            delivered_at TIMESTAMP COMMENT '送达时间',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            FOREIGN KEY (order_id) REFERENCES apexflow_order(id)
                        )
                    """);

            // 9. 创建商品评价表（依赖订单和商品）
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS apexflow_review (
                            id INT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
                            order_id VARCHAR(50) NOT NULL COMMENT '关联订单号',
                            product_id INT NOT NULL COMMENT '商品ID',
                            user_id INT NOT NULL COMMENT '评价用户ID',
                            rating TINYINT NOT NULL COMMENT '评分：1-5星',
                            content TEXT COMMENT '评价内容',
                            images TEXT COMMENT '评价图片URL，多个用逗号分隔',
                            is_anonymous BOOLEAN DEFAULT FALSE COMMENT '是否匿名评价',
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '评价时间',
                            FOREIGN KEY (order_id) REFERENCES apexflow_order(id),
                            FOREIGN KEY (product_id) REFERENCES apexflow_product(id)
                        )
                    """);

            logger.info("All tables created successfully");
        }
    }

    private static void insertTestData(Connection conn) throws Exception {
        try (Statement stmt = conn.createStatement()) {
            // 1. 插入用户数据
            stmt.execute("INSERT INTO apexflow_system_user (username, password_hash, salt, real_name, email, phone, is_admin, status) VALUES " +
                    "('admin', '$2a$10$AbCdEfGhIjKlMnOpQrStUvWxYz1234567890', 'a1b2c3d4e5f678901234567890123456', '系统管理员', 'admin@apexflow.com', '13800138000', TRUE, 1)");

            // 2. 插入商品数据（简化版，只插入5个用于测试）
            stmt.execute("""
                        INSERT INTO apexflow_product (name, category, price, stock, status) VALUES
                        ('iPhone 14 Pro', '手机', 7999.00, 100, 1),
                        ('MacBook Pro 16英寸', '电脑', 18999.00, 50, 1),
                        ('华为Mate 50', '手机', 4999.00, 150, 1),
                        ('小米13', '手机', 3999.00, 200, 1),
                        ('戴尔XPS 13', '电脑', 8999.00, 80, 1)
                    """);

            // 3. 插入订单数据（简化版，只插入用于测试的数据）
            stmt.execute("""
                        INSERT INTO apexflow_order (id, user_id, total_amount, status, payment_method, created_at, paid_at) VALUES
                        ('ORDER20231201001', 1001, 7999.00, 4, 'alipay', '2023-12-01 09:00:00', '2023-12-01 09:05:00'),
                        ('ORDER20231201002', 1002, 18999.00, 4, 'wxpay', '2023-12-01 09:30:00', '2023-12-01 09:35:00'),
                        ('ORDER20231201003', 1003, 4999.00, 3, 'alipay', '2023-12-01 10:00:00', '2023-12-01 10:05:00'),
                        ('ORDER20231201004', 1004, 3999.00, 4, 'wxpay', '2023-12-01 10:30:00', '2023-12-01 10:35:00'),
                        ('ORDER20231201005', 1005, 8999.00, 2, 'alipay', '2023-12-01 11:00:00', '2023-12-01 11:05:00')
                    """);

            // 4. 插入订单项数据
            stmt.execute("""
                        INSERT INTO apexflow_order_item (order_id, product_id, product_name, quantity, price, subtotal) VALUES
                        ('ORDER20231201001', 1, 'iPhone 14 Pro', 1, 7999.00, 7999.00),
                        ('ORDER20231201002', 2, 'MacBook Pro 16英寸', 1, 18999.00, 18999.00),
                        ('ORDER20231201003', 3, '华为Mate 50', 1, 4999.00, 4999.00),
                        ('ORDER20231201004', 4, '小米13', 1, 3999.00, 3999.00),
                        ('ORDER20231201005', 5, '戴尔XPS 13', 1, 8999.00, 8999.00)
                    """);

            logger.info("Test data inserted successfully");
        }
    }

    /**
     * 清空所有表数据（用于测试清理）
     */
    public static void clearAllTables(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET REFERENTIAL_INTEGRITY FALSE");

            // 按依赖关系的逆序清空表
            String[] tables = {
                    "apexflow_review",
                    "apexflow_logistics",
                    "apexflow_income",
                    "apexflow_after_sales",
                    "apexflow_inventory_log",
                    "apexflow_order_item",
                    "apexflow_order",
                    "apexflow_product",
                    "apexflow_system_user"
            };

            for (String table : tables) {
                stmt.execute("DELETE FROM " + table);
            }

            stmt.execute("SET REFERENTIAL_INTEGRITY TRUE");
            logger.info("All tables cleared successfully");
        } catch (Exception e) {
            logger.error("Failed to clear tables", e);
        }
    }
}
