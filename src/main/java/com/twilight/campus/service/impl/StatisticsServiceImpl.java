package com.twilight.campus.service.impl;

import com.twilight.campus.mapper.StatisticsMapper;
import com.twilight.campus.service.StatisticsService;
import com.twilight.campus.vo.DashboardStatVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 数据统计业务实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {

    @Autowired
    private StatisticsMapper statisticsMapper;

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
        vo.setHotContentList(statisticsMapper.selectHotContentList());

        return vo;
    }
}
