package com.twilight.campus.task;

import com.twilight.campus.mapper.ContentMapper;
import com.twilight.campus.service.LikeCounterBufferService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "campus.cache", name = "like-write-behind-enabled", havingValue = "true")
public class LikeCounterFlushTask {

    private final LikeCounterBufferService likeCounterBufferService;
    private final ContentMapper contentMapper;

    public LikeCounterFlushTask(LikeCounterBufferService likeCounterBufferService, ContentMapper contentMapper) {
        this.likeCounterBufferService = likeCounterBufferService;
        this.contentMapper = contentMapper;
    }

    /**
     * Redis 点赞缓冲刷库任务。生产环境更大规模时可改成 Redis Stream 或 MQ。
     */
    @Scheduled(fixedDelayString = "${campus.cache.counter-flush-delay-ms:30000}")
    public void flushLikeDeltas() {
        Set<String> ids = likeCounterBufferService.pendingContentIds();
        for (String idText : ids) {
            try {
                Long contentId = Long.valueOf(idText);
                Integer delta = likeCounterBufferService.getDelta(contentId);
                if (delta == null || delta == 0) {
                    likeCounterBufferService.clearDelta(contentId);
                    continue;
                }
                contentMapper.updateLikeCountByDelta(contentId, delta);
                likeCounterBufferService.clearDelta(contentId);
            } catch (Exception e) {
                log.warn("Flush like delta failed, contentId={}", idText, e);
            }
        }
    }
}
