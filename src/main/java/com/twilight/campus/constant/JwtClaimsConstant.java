package com.twilight.campus.constant;

/**
 * JWT相关常量
 */
public class JwtClaimsConstant {

    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String ROLE_CODE = "roleCode";

    // token签名密钥
    public static final String JWT_SECRET_KEY = "twilight_campus_jwt_secret_key_2026_very_secure_123456";


    // token过期时间，单位：毫秒
    public static final Long JWT_TTL = 24L * 60 * 60 * 1000;
}
