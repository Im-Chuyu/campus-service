package com.twilight.campus.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilight.campus.config.CacheProperties;
import com.twilight.campus.constant.RedisKeyConstant;
import com.twilight.campus.mapper.StatisticsMapper;
import com.twilight.campus.service.OptionalRedisService;
import com.twilight.campus.service.StatisticsService;
import com.twilight.campus.vo.DashboardStatVO;
import com.twilight.campus.vo.HotContentVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

/**
 * 数据统计业务实现类
 */
@Slf4j
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

    @Autowired
    private OptionalRedisService redisService;

    @Autowired
    private CacheProperties cacheProperties;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public DashboardStatVO getDashboardStat() {
        Long userTotal = statisticsMapper.countUsers();
        Long contentTotal = statisticsMapper.countContents();
        Long categoryTotal = statisticsMapper.countCategories();
        Long todayContentTotal = statisticsMapper.countTodayContents();
        Long auditedContentTotal = statisticsMapper.countAuditedContents();
        Long passedContentTotal = statisticsMapper.countPassedContents();

        Long auditPassRate = 0L;
        if (auditedContentTotal != null && auditedContentTotal > 0) {
            auditPassRate = passedContentTotal * 100 / auditedContentTotal;
        }

        DashboardStatVO vo = new DashboardStatVO();
        vo.setUserTotal(userTotal);
        vo.setContentTotal(contentTotal);
        vo.setCategoryTotal(categoryTotal);
        vo.setTodayContentTotal(todayContentTotal);
        vo.setAuditedContentTotal(auditedContentTotal);
        vo.setAuditPassRate(auditPassRate);
        vo.setHotContentList(getHotContentList());

        return vo;
    }

    private List<HotContentVO> getHotContentList() {
        String cached = redisService.get(RedisKeyConstant.HOT_CONTENT_LIST);
        if (cached != null && !cached.isEmpty()) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<HotContentVO>>() {});
            } catch (Exception e) {
                log.warn("Read hot content cache failed", e);
                redisService.delete(RedisKeyConstant.HOT_CONTENT_LIST);
            }
        }

        List<HotContentVO> hotContentList = statisticsMapper.selectHotContentList();
        // Redis 开启后热门内容短缓存 60 秒左右，降低后台首页/热门榜频繁刷新造成的数据库排序压力。
        if (redisService.enabled()) {
            try {
                redisService.set(
                    RedisKeyConstant.HOT_CONTENT_LIST,
                    objectMapper.writeValueAsString(hotContentList),
                    Duration.ofSeconds(cacheProperties.getHotContentTtlSeconds())
                );
            } catch (Exception e) {
                log.warn("Write hot content cache failed", e);
            }
        }
        return hotContentList;
    }
}
