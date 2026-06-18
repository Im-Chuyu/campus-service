package com.twilight.campus.service;

import com.twilight.campus.config.CacheProperties;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
public class ContentCounterBufferService {

    private static final String VIEW_DELTA_IDS_KEY = "content:view:delta:ids";
    private static final String VIEW_DELTA_PREFIX = "content:view:delta:";
    private static final String LIKE_DELTA_IDS_KEY = "content:like:delta:ids";
    private static final String LIKE_DELTA_PREFIX = "content:like:delta:";

    private final CacheProperties cacheProperties;
    private final OptionalRedisService redisService;

    public ContentCounterBufferService(CacheProperties cacheProperties, OptionalRedisService redisService) {
        this.cacheProperties = cacheProperties;
        this.redisService = redisService;
    }

    public boolean recordContentViewDelta(Long contentId, int delta) {
        if (!cacheProperties.isViewWriteBehindEnabled()) {
            return false;
        }
        return recordDelta(VIEW_DELTA_IDS_KEY, VIEW_DELTA_PREFIX, contentId, delta);
    }

    public Set<String> pendingViewContentIds() {
        return redisService.members(VIEW_DELTA_IDS_KEY);
    }

    public Integer getViewDelta(Long contentId) {
        return getDelta(VIEW_DELTA_PREFIX, contentId);
    }

    public void clearViewDelta(Long contentId) {
        clearDelta(VIEW_DELTA_IDS_KEY, VIEW_DELTA_PREFIX, contentId);
    }

    public boolean recordContentLikeDelta(Long contentId, int delta) {
        if (!cacheProperties.isLikeWriteBehindEnabled()) {
            return false;
        }
        return recordDelta(LIKE_DELTA_IDS_KEY, LIKE_DELTA_PREFIX, contentId, delta);
    }

    public Set<String> pendingLikeContentIds() {
        return redisService.members(LIKE_DELTA_IDS_KEY);
    }

    public Integer getLikeDelta(Long contentId) {
        return getDelta(LIKE_DELTA_PREFIX, contentId);
    }

    public void clearLikeDelta(Long contentId) {
        clearDelta(LIKE_DELTA_IDS_KEY, LIKE_DELTA_PREFIX, contentId);
    }

    private boolean recordDelta(String idsKey, String deltaPrefix, Long contentId, int delta) {
        if (!redisService.enabled() || contentId == null || delta == 0) {
            return false;
        }
        Long value = redisService.increment(deltaPrefix + contentId, delta, Duration.ofHours(6));
        if (value == null) {
            return false;
        }
        redisService.addToSet(idsKey, String.valueOf(contentId));
        return true;
    }

    private Integer getDelta(String deltaPrefix, Long contentId) {
        String value = redisService.get(deltaPrefix + contentId);
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void clearDelta(String idsKey, String deltaPrefix, Long contentId) {
        redisService.delete(deltaPrefix + contentId);
        redisService.removeFromSet(idsKey, String.valueOf(contentId));
    }
}
