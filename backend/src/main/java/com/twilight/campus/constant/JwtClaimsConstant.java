package com.twilight.campus.constant;

/**
 * JWT相关常量
 */
public class JwtClaimsConstant {

    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String ROLE_CODE = "roleCode";
    public static final String DEVICE_ID = "deviceId";

    // token过期时间，单位：毫秒，默认7天。
    public static final Long JWT_TTL = 7L * 24 * 60 * 60 * 1000;
}
