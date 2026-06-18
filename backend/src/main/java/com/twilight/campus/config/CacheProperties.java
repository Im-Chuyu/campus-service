package com.twilight.campus.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "campus.cache")
public class CacheProperties {

    private boolean redisEnabled = false;

    private long hotContentTtlSeconds = 60;

    private long loginRateLimitWindowSeconds = 60;

    private int loginRateLimitMaxAttempts = 10;

    private long counterFlushDelayMs = 30000;

    private boolean likeWriteBehindEnabled = false;

    private boolean viewWriteBehindEnabled = false;
}
