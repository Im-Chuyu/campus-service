package com.twilight.campus.utils;

import com.twilight.campus.constant.JwtClaimsConstant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
public class JwtUtil {

    /**
     * 获取签名密钥
     */
    private static SecretKey getSignKey() {
        byte[] keyBytes = JwtClaimsConstant.JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成token
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param roleCode 角色编码
     * @return token
     */
    public static String createToken(Long userId, String username, String roleCode) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.USER_ID, userId);
        claims.put(JwtClaimsConstant.USERNAME, username);
        claims.put(JwtClaimsConstant.ROLE_CODE, roleCode);

        JwtBuilder builder = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JwtClaimsConstant.JWT_TTL))
                .signWith(getSignKey(), SignatureAlgorithm.HS256);

        return builder.compact();
    }

    /**
     * 解析token
     *
     * @param token token字符串
     * @return Claims
     */
    public static Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 从token中获取用户ID
     */
    public static Long getUserId(String token) {
        Claims claims = parseToken(token);
        Object userIdObj = claims.get(JwtClaimsConstant.USER_ID);
        if (userIdObj instanceof Integer) {
            return ((Integer) userIdObj).longValue();
        }
        if (userIdObj instanceof Long) {
            return (Long) userIdObj;
        }
        return Long.valueOf(userIdObj.toString());
    }

    /**
     * 从token中获取用户名
     */
    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        return claims.get(JwtClaimsConstant.USERNAME, String.class);
    }

    /**
     * 从token中获取角色编码
     */
    public static String getRoleCode(String token) {
        Claims claims = parseToken(token);
        return claims.get(JwtClaimsConstant.ROLE_CODE, String.class);
    }
}
