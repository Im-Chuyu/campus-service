package com.twilight.campus.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 */
@Component
public class RedisUtil {

    private static StringRedisTemplate stringRedisTemplate;

    public RedisUtil(StringRedisTemplate template) {
        RedisUtil.stringRedisTemplate = template;
    }

    public static void set(String key, String value, long timeout, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public static String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    public static void delete(String key) {
        stringRedisTemplate.delete(key);
    }
}
