package com.apex.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;

public class PasswordUtil {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);

    // 用于生成32位盐值的字符集
    private static final String SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * 生成32位随机盐值（符合apexflow_system_user表结构要求）
     */
    public static String generateSalt() {
        StringBuilder salt = new StringBuilder();
        Random random = new SecureRandom();

        for (int i = 0; i < 32; i++) {
            salt.append(SALT_CHARS.charAt(random.nextInt(SALT_CHARS.length())));
        }

        String saltStr = salt.toString();
        logger.debug("Generated 32-character salt: {}", saltStr);
        return saltStr;
    }

    /**
     * 生成盐值（Base64编码版本，兼容旧代码）
     */
    public static String generateSaltBase64() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[24]; // 24字节生成32位Base64字符串
        random.nextBytes(salt);
        String saltStr = Base64.getEncoder().encodeToString(salt);
        logger.debug("Generated Base64 salt: {}...", saltStr.substring(0, Math.min(10, saltStr.length())));
        return saltStr;
    }

    /**
     * 哈希密码（使用SHA-256）
     */
    public static String hashPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            // 将盐和密码组合
            String combined = salt + password;
            byte[] hashedBytes = md.digest(combined.getBytes());
            String hash = Base64.getEncoder().encodeToString(hashedBytes);
            logger.debug("Password hashed with salt");
            return hash;
        } catch (NoSuchAlgorithmException e) {
            logger.error("SHA-256 algorithm not available", e);
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * 验证密码
     */
    public static boolean verifyPassword(String password, String salt, String storedHash) {
        String hashedInput = hashPassword(password, salt);
        boolean matches = hashedInput.equals(storedHash);
        logger.debug("Password verification result: {}", matches ? "SUCCESS" : "FAILED");
        return matches;
    }

    /**
     * 生成随机密码（12位）
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(12);
    }

    /**
     * 生成指定长度的随机密码
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        logger.debug("Generated random password ({} characters)", length);
        return password.toString();
    }

    /**
     * 生成强密码（确保包含大小写字母、数字和特殊字符）
     */
    public static String generateStrongPassword(int length) {
        String upperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialChars = "!@#$%^&*";
        String allChars = upperCase + lowerCase + numbers + specialChars;

        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        // 确保每种类型至少有一个字符
        password.append(upperCase.charAt(random.nextInt(upperCase.length())));
        password.append(lowerCase.charAt(random.nextInt(lowerCase.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialChars.charAt(random.nextInt(specialChars.length())));

        // 填充剩余长度
        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        // 随机打乱
        String passwordStr = shuffleString(password.toString());
        logger.debug("Generated strong password ({} characters)", length);
        return passwordStr;
    }

    /**
     * 随机打乱字符串
     */
    private static String shuffleString(String input) {
        char[] characters = input.toCharArray();
        SecureRandom random = new SecureRandom();

        for (int i = characters.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = characters[index];
            characters[index] = characters[i];
            characters[i] = temp;
        }

        return new String(characters);
    }
}