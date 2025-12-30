package com.apex.util;

import com.apex.core.model.SystemUser;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTokenUtil {

    private static final String SECRET_KEY = "apexflow-secret-key-2024-application-security-token";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
    private static final long EXPIRATION_TIME = 86400000; // 24小时

    /**
     * 生成JWT令牌
     */
    public static String generateToken(SystemUser user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("isAdmin", user.getAdmin() != null && user.getAdmin());
        claims.put("permissions", PermissionUtil.getUserPermissions(user));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 解析JWT令牌
     */
    public static Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException e) {
            return null;
        }
    }

    /**
     * 验证令牌是否有效
     */
    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && !isTokenExpired(claims);
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * 检查令牌是否过期
     */
    public static boolean isTokenExpired(Claims claims) {
        return claims.getExpiration().before(new Date());
    }

    /**
     * 从令牌中获取用户ID
     */
    public static Integer getUserId(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.get("userId", Integer.class) : null;
    }

    /**
     * 从令牌中获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    /**
     * 从令牌中获取权限列表
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Boolean> getPermissions(String token) {
        Claims claims = parseToken(token);
        if (claims != null) {
            return (Map<String, Boolean>) claims.get("permissions");
        }
        return null;
    }

    /**
     * 检查用户是否拥有指定权限
     */
    public static boolean hasPermission(String token, Permission permission) {
        Map<String, Boolean> permissions = getPermissions(token);
        if (permissions == null) return false;

        // 管理员拥有所有权限
        if (Boolean.TRUE.equals(permissions.get("isAdmin"))) {
            return true;
        }

        // 检查具体权限
        return switch (permission) {
            case ORDER_MANAGE -> Boolean.TRUE.equals(permissions.get("canManageOrder"));
            case LOGISTICS_MANAGE -> Boolean.TRUE.equals(permissions.get("canManageLogistics"));
            case AFTER_SALES_MANAGE -> Boolean.TRUE.equals(permissions.get("canManageAfterSales"));
            case REVIEW_MANAGE -> Boolean.TRUE.equals(permissions.get("canManageReview"));
            case INVENTORY_MANAGE -> Boolean.TRUE.equals(permissions.get("canManageInventory"));
            case INCOME_MANAGE -> Boolean.TRUE.equals(permissions.get("canManageIncome"));
            case ADMIN -> Boolean.TRUE.equals(permissions.get("isAdmin"));
        };
    }
}