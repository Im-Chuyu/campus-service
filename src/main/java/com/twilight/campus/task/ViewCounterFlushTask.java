package com.twilight.campus.task;

import com.twilight.campus.mapper.ContentMapper;
import com.twilight.campus.service.ContentCounterBufferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "campus.cache", name = "view-write-behind-enabled", havingValue = "true")
public class ViewCounterFlushTask {

    private final ContentCounterBufferService counterBufferService;
    private final ContentMapper contentMapper;

    public ViewCounterFlushTask(ContentCounterBufferService counterBufferService, ContentMapper contentMapper) {
        this.counterBufferService = counterBufferService;
        this.contentMapper = contentMapper;
    }

    @Scheduled(fixedDelayString = "${campus.cache.counter-flush-delay-ms:30000}")
    public void flushViewDeltas() {
        Set<String> ids = counterBufferService.pendingViewContentIds();
        for (String idText : ids) {
            try {
                Long contentId = Long.valueOf(idText);
                Integer delta = counterBufferService.getViewDelta(contentId);
                if (delta == null || delta == 0) {
                    counterBufferService.clearViewDelta(contentId);
                    continue;
                }
                contentMapper.updateViewCountByDelta(contentId, delta);
                counterBufferService.clearViewDelta(contentId);
            } catch (Exception e) {
                log.warn("Flush view delta failed, contentId={}", idText, e);
            }
        }
    }
}
