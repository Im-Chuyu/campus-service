package com.twilight.campus.service;

import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LikeCounterBufferService {

    private final ContentCounterBufferService counterBufferService;

    public LikeCounterBufferService(ContentCounterBufferService counterBufferService) {
        this.counterBufferService = counterBufferService;
    }

    public boolean recordContentLikeDelta(Long contentId, int delta) {
        return counterBufferService.recordContentLikeDelta(contentId, delta);
    }

    public Set<String> pendingContentIds() {
        return counterBufferService.pendingLikeContentIds();
    }

    public Integer getDelta(Long contentId) {
        return counterBufferService.getLikeDelta(contentId);
    }

    public void clearDelta(Long contentId) {
        counterBufferService.clearLikeDelta(contentId);
    }
}
