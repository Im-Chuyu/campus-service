package com.twilight.campus.service;

import com.twilight.campus.config.CacheProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OptionalRedisService {

    private final CacheProperties cacheProperties;
    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;

    public OptionalRedisService(CacheProperties cacheProperties,
                                ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.cacheProperties = cacheProperties;
        this.redisTemplateProvider = redisTemplateProvider;
    }

    public boolean enabled() {
        return cacheProperties.isRedisEnabled() && redisTemplateProvider.getIfAvailable() != null;
    }

    public String get(String key) {
        if (!enabled()) {
            return null;
        }
        try {
            return redisTemplateProvider.getObject().opsForValue().get(key);
        } catch (Exception e) {
            log.warn("Redis get failed, key={}", key, e);
            return null;
        }
    }

    public void set(String key, String value, Duration ttl) {
        if (!enabled()) {
            return;
        }
        try {
            redisTemplateProvider.getObject().opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.warn("Redis set failed, key={}", key, e);
        }
    }

    public Long increment(String key, long delta, Duration ttlWhenFirstCreated) {
        if (!enabled()) {
            return null;
        }
        try {
            StringRedisTemplate redisTemplate = redisTemplateProvider.getObject();
            Long value = redisTemplate.opsForValue().increment(key, delta);
            if (value != null && Math.abs(value) == Math.abs(delta) && ttlWhenFirstCreated != null) {
                redisTemplate.expire(key, ttlWhenFirstCreated);
            }
            return value;
        } catch (Exception e) {
            log.warn("Redis increment failed, key={}", key, e);
            return null;
        }
    }

    public void delete(String key) {
        if (!enabled()) {
            return;
        }
        try {
            redisTemplateProvider.getObject().delete(key);
        } catch (Exception e) {
            log.warn("Redis delete failed, key={}", key, e);
        }
    }

    public void addToSet(String key, String value) {
        if (!enabled()) {
            return;
        }
        try {
            redisTemplateProvider.getObject().opsForSet().add(key, value);
        } catch (Exception e) {
            log.warn("Redis set add failed, key={}", key, e);
        }
    }

    public Set<String> members(String key) {
        if (!enabled()) {
            return Collections.emptySet();
        }
        try {
            Set<String> members = redisTemplateProvider.getObject().opsForSet().members(key);
            return members == null ? Collections.emptySet() : members;
        } catch (Exception e) {
            log.warn("Redis set members failed, key={}", key, e);
            return Collections.emptySet();
        }
    }

    public void removeFromSet(String key, String value) {
        if (!enabled()) {
            return;
        }
        try {
            redisTemplateProvider.getObject().opsForSet().remove(key, value);
        } catch (Exception e) {
            log.warn("Redis set remove failed, key={}", key, e);
        }
    }

    public void addToZSet(String key, String value, double score, Duration ttl) {
        if (!enabled()) {
            return;
        }
        try {
            StringRedisTemplate redisTemplate = redisTemplateProvider.getObject();
            redisTemplate.opsForZSet().add(key, value, score);
            if (ttl != null) {
                redisTemplate.expire(key, ttl);
            }
        } catch (Exception e) {
            log.warn("Redis zset add failed, key={}", key, e);
        }
    }

    public Set<String> zRange(String key, long start, long end) {
        if (!enabled()) {
            return Collections.emptySet();
        }
        try {
            Set<String> values = redisTemplateProvider.getObject().opsForZSet().range(key, start, end);
            return values == null ? Collections.emptySet() : values;
        } catch (Exception e) {
            log.warn("Redis zset range failed, key={}", key, e);
            return Collections.emptySet();
        }
    }

    public Long zCard(String key) {
        if (!enabled()) {
            return 0L;
        }
        try {
            Long count = redisTemplateProvider.getObject().opsForZSet().zCard(key);
            return count == null ? 0L : count;
        } catch (Exception e) {
            log.warn("Redis zset card failed, key={}", key, e);
            return 0L;
        }
    }

    public void removeFromZSet(String key, String value) {
        if (!enabled()) {
            return;
        }
        try {
            redisTemplateProvider.getObject().opsForZSet().remove(key, value);
        } catch (Exception e) {
            log.warn("Redis zset remove failed, key={}", key, e);
        }
    }

    public void trimZSetToLatest(String key, long maxSize) {
        if (!enabled() || maxSize < 0) {
            return;
        }
        try {
            StringRedisTemplate redisTemplate = redisTemplateProvider.getObject();
            Long count = redisTemplate.opsForZSet().zCard(key);
            if (count != null && count > maxSize) {
                redisTemplate.opsForZSet().removeRange(key, 0, count - maxSize - 1);
            }
        } catch (Exception e) {
            log.warn("Redis zset trim failed, key={}", key, e);
        }
    }

    public Long getExpireSeconds(String key) {
        if (!enabled()) {
            return null;
        }
        try {
            return redisTemplateProvider.getObject().getExpire(key, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("Redis get expire failed, key={}", key, e);
            return null;
        }
    }
}
