package com.apex.util;

import java.util.Scanner;

/**
 * 密码哈希生成工具
 * 用于在控制台生成密码的盐和哈希值，方便调试和数据库初始化
 * 适配 apexflow_system_user 表结构
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("     密码哈希生成工具 v1.1");
        System.out.println("    适配 apexflow_system_user 表");
        System.out.println("========================================");
        System.out.println();

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("请选择操作：");
            System.out.println("1. 为输入的密码生成盐和哈希");
            System.out.println("2. 生成随机密码并计算哈希");
            System.out.println("3. 批量生成多个哈希");
            System.out.println("4. 生成完整的用户SQL语句");
            System.out.println("5. 生成管理员账户SQL");
            System.out.println("6. 验证密码哈希");
            System.out.println("0. 退出");
            System.out.print("请输入选项 (0-6): ");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    hashInputPassword(scanner);
                    break;
                case "2":
                    generateRandomPasswordAndHash(scanner);
                    break;
                case "3":
                    batchGenerateHashes(scanner);
                    break;
                case "4":
                    generateCompleteUserSQL(scanner);
                    break;
                case "5":
                    generateAdminUserSQL(scanner);
                    break;
                case "6":
                    verifyPasswordHash(scanner);
                    break;
                case "0":
                    System.out.println("感谢使用，再见！");
                    scanner.close();
                    return;
                default:
                    System.out.println("无效选项，请重新输入！\n");
            }
        }
    }

    /**
     * 为输入的密码生成盐和哈希
     */
    private static void hashInputPassword(Scanner scanner) {
        System.out.println("\n========== 单次密码哈希生成 ==========");

        System.out.print("请输入用户名: ");
        String username = scanner.nextLine().trim();

        System.out.print("请输入密码: ");
        String password = scanner.nextLine();

        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("错误：用户名和密码不能为空！\n");
            return;
        }

        // 生成32位随机盐
        String salt = PasswordUtil.generateSalt();

        // 计算哈希
        String hash = PasswordUtil.hashPassword(password, salt);

        // 显示结果
        displayResults(username, password, salt, hash);
    }

    /**
     * 生成随机密码并计算哈希
     */
    private static void generateRandomPasswordAndHash(Scanner scanner) {
        System.out.println("\n========== 随机密码生成 ==========");

        System.out.print("请输入用户名: ");
        String username = scanner.nextLine().trim();

        System.out.print("请输入要生成的密码长度 (默认12位): ");
        String lengthInput = scanner.nextLine();

        int length = 12;
        try {
            if (!lengthInput.trim().isEmpty()) {
                length = Integer.parseInt(lengthInput.trim());
                if (length < 8) {
                    System.out.println("密码长度至少8位，已自动调整为8位");
                    length = 8;
                } else if (length > 32) {
                    System.out.println("密码长度过长，已自动调整为32位");
                    length = 32;
                }
            }
        } catch (NumberFormatException e) {
            System.out.println("输入不是有效数字，使用默认长度12位");
        }

        System.out.print("生成强密码？(y/N): ");
        String strongChoice = scanner.nextLine().trim().toLowerCase();

        String password;
        if (strongChoice.equals("y") || strongChoice.equals("yes")) {
            password = PasswordUtil.generateStrongPassword(length);
        } else {
            password = PasswordUtil.generateRandomPassword(length);
        }

        // 生成32位随机盐
        String salt = PasswordUtil.generateSalt();

        // 计算哈希
        String hash = PasswordUtil.hashPassword(password, salt);

        System.out.println("已生成随机密码，请妥善保管！");
        displayResults(username, password, salt, hash);
    }

    /**
     * 批量生成多个哈希
     */
    private static void batchGenerateHashes(Scanner scanner) {
        System.out.println("\n========== 批量密码哈希生成 ==========");

        System.out.print("请输入要生成的密码数量 (1-10): ");
        String countInput = scanner.nextLine();

        int count = 3;
        try {
            if (!countInput.trim().isEmpty()) {
                count = Integer.parseInt(countInput.trim());
                if (count < 1) count = 1;
                if (count > 10) count = 10;
            }
        } catch (NumberFormatException e) {
            System.out.println("输入不是有效数字，使用默认数量3个");
        }

        System.out.print("生成强密码？(y/N): ");
        String strongChoice = scanner.nextLine().trim().toLowerCase();
        boolean generateStrong = strongChoice.equals("y") || strongChoice.equals("yes");

        System.out.println();
        for (int i = 1; i <= count; i++) {
            System.out.println("--- 用户 #" + i + " ---");

            System.out.print("请输入用户名" + i + ": ");
            String username = scanner.nextLine().trim();

            // 生成密码
            String password;
            if (generateStrong) {
                password = PasswordUtil.generateStrongPassword(12);
            } else {
                password = PasswordUtil.generateRandomPassword(12);
            }

            System.out.println("生成的密码: " + password);

            // 生成盐和哈希
            String salt = PasswordUtil.generateSalt();
            String hash = PasswordUtil.hashPassword(password, salt);

            // 显示结果
            System.out.println("用户名: " + username);
            System.out.println("密码: " + password);
            System.out.println("盐值: " + salt);
            System.out.println("哈希值: " + hash);
            System.out.println();
        }

        System.out.println("批量生成完成！\n");
    }

    /**
     * 生成完整的用户SQL语句
     */
    private static void generateCompleteUserSQL(Scanner scanner) {
        System.out.println("\n========== 生成完整用户SQL ==========");

        System.out.print("请输入用户名: ");
        String username = scanner.nextLine().trim();

        System.out.print("请输入真实姓名: ");
        String realName = scanner.nextLine().trim();

        System.out.print("请输入邮箱: ");
        String email = scanner.nextLine().trim();

        System.out.print("请输入手机号: ");
        String phone = scanner.nextLine().trim();

        System.out.print("请输入密码: ");
        String password = scanner.nextLine();

        System.out.println("\n请设置权限 (输入y表示启用，n表示禁用):");
        System.out.print("超级管理员 (is_admin): ");
        boolean isAdmin = scanner.nextLine().trim().toLowerCase().startsWith("y");

        System.out.print("订单管理权限 (can_manage_order): ");
        boolean canManageOrder = scanner.nextLine().trim().toLowerCase().startsWith("y");

        System.out.print("物流管理权限 (can_manage_logistics): ");
        boolean canManageLogistics = scanner.nextLine().trim().toLowerCase().startsWith("y");

        System.out.print("售后管理权限 (can_manage_after_sales): ");
        boolean canManageAfterSales = scanner.nextLine().trim().toLowerCase().startsWith("y");

        System.out.print("评价管理权限 (can_manage_review): ");
        boolean canManageReview = scanner.nextLine().trim().toLowerCase().startsWith("y");

        System.out.print("库存管理权限 (can_manage_inventory): ");
        boolean canManageInventory = scanner.nextLine().trim().toLowerCase().startsWith("y");

        System.out.print("收入管理权限 (can_manage_income): ");
        boolean canManageIncome = scanner.nextLine().trim().toLowerCase().startsWith("y");

        System.out.print("状态 (1-正常, 0-禁用): ");
        int status = 1;
        String statusInput = scanner.nextLine().trim();
        if (!statusInput.isEmpty()) {
            status = statusInput.equals("0") ? 0 : 1;
        }

        // 生成盐和哈希
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        // 生成SQL语句
        System.out.println("\n========== 生成的SQL语句 ==========");

        String sql = String.format(
                "INSERT INTO apexflow_system_user (username, password_hash, salt, real_name, email, phone, " +
                        "is_admin, can_manage_order, can_manage_logistics, can_manage_after_sales, " +
                        "can_manage_review, can_manage_inventory, can_manage_income, status) VALUES\n" +
                        "('%s', '%s', '%s', %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %d);",
                username,
                hash,
                salt,
                realName.isEmpty() ? "NULL" : "'" + realName + "'",
                email.isEmpty() ? "NULL" : "'" + email + "'",
                phone.isEmpty() ? "NULL" : "'" + phone + "'",
                isAdmin ? "TRUE" : "FALSE",
                canManageOrder ? "TRUE" : "FALSE",
                canManageLogistics ? "TRUE" : "FALSE",
                canManageAfterSales ? "TRUE" : "FALSE",
                canManageReview ? "TRUE" : "FALSE",
                canManageInventory ? "TRUE" : "FALSE",
                canManageIncome ? "TRUE" : "FALSE",
                status
        );

        System.out.println(sql);
        System.out.println("\nSQL语句已生成！\n");
    }

    /**
     * 生成管理员账户SQL
     */
    private static void generateAdminUserSQL(Scanner scanner) {
        System.out.println("\n========== 生成管理员账户SQL ==========");

        System.out.print("请输入管理员用户名 (默认admin): ");
        String username = scanner.nextLine().trim();
        if (username.isEmpty()) {
            username = "admin";
        }

        System.out.print("请输入管理员密码 (默认admin123): ");
        String password = scanner.nextLine().trim();
        if (password.isEmpty()) {
            password = "admin123";
        }

        System.out.print("请输入真实姓名 (默认系统管理员): ");
        String realName = scanner.nextLine().trim();
        if (realName.isEmpty()) {
            realName = "系统管理员";
        }

        System.out.print("请输入邮箱 (默认admin@apexflow.com): ");
        String email = scanner.nextLine().trim();
        if (email.isEmpty()) {
            email = "admin@apexflow.com";
        }

        System.out.print("请输入手机号 (默认13800138000): ");
        String phone = scanner.nextLine().trim();
        if (phone.isEmpty()) {
            phone = "13800138000";
        }

        // 生成盐和哈希
        String salt = PasswordUtil.generateSalt();
        String hash = PasswordUtil.hashPassword(password, salt);

        // 生成SQL语句
        System.out.println("\n========== 管理员账户SQL语句 ==========");

        String sql = String.format(
                """
                        -- 创建超级管理员账户
                        INSERT INTO apexflow_system_user (username, password_hash, salt, real_name, email, phone, \
                        is_admin, can_manage_order, can_manage_logistics, can_manage_after_sales, \
                        can_manage_review, can_manage_inventory, can_manage_income, status) VALUES
                        ('%s', '%s', '%s', '%s', '%s', '%s', TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, TRUE, 1);
                        
                        -- 测试用的普通物流管理员
                        INSERT INTO apexflow_system_user (username, password_hash, salt, real_name, email, phone, \
                        is_admin, can_manage_order, can_manage_logistics, can_manage_after_sales, \
                        can_manage_review, can_manage_inventory, can_manage_income, status) VALUES
                        ('logistics_admin', '%s', '%s', '物流管理员', 'logistics@apexflow.com', '13900139000', \
                        FALSE, TRUE, TRUE, FALSE, FALSE, FALSE, FALSE, 1);""",
                username, hash, salt, realName, email, phone,
                hash, salt  // 使用相同的哈希和盐值，但实际中应为不同的
        );

        System.out.println(sql);
        System.out.println("\nSQL语句已生成！\n");
    }

    /**
     * 验证密码哈希
     */
    private static void verifyPasswordHash(Scanner scanner) {
        System.out.println("\n========== 验证密码哈希 ==========");

        System.out.print("请输入密码: ");
        String password = scanner.nextLine();

        System.out.print("请输入盐值: ");
        String salt = scanner.nextLine();

        System.out.print("请输入期望的哈希值: ");
        String expectedHash = scanner.nextLine();

        String actualHash = PasswordUtil.hashPassword(password, salt);
        boolean matches = actualHash.equals(expectedHash);

        System.out.println("\n========== 验证结果 ==========");
        System.out.println("输入密码: " + password);
        System.out.println("盐值: " + salt);
        System.out.println("期望哈希: " + expectedHash);
        System.out.println("实际哈希: " + actualHash);
        System.out.println("验证结果: " + (matches ? "✓ 匹配成功" : "✗ 匹配失败"));

        if (matches) {
            System.out.println("密码验证通过！");
        } else {
            System.out.println("密码验证失败！");
        }
        System.out.println("==============================\n");
    }

    /**
     * 显示结果
     */
    private static void displayResults(String username, String password, String salt, String hash) {
        System.out.println("\n========== 生成结果 ==========");
        System.out.println("用户名: " + username);
        System.out.println("原始密码: " + password);
        System.out.println("盐值 (32位): " + salt);
        System.out.println("哈希值: " + hash);
        System.out.println("================================\n");

        // 输出SQL插入语句示例
        System.out.println("===== SQL 插入语句示例 =====");
        System.out.println("INSERT INTO apexflow_system_user (username, password_hash, salt)");
        System.out.println("VALUES ('" + username + "', '" + hash + "', '" + salt + "');");
        System.out.println("==============================\n");
    }
}
