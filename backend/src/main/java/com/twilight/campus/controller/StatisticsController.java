package com.twilight.campus.controller;

import com.twilight.campus.result.Result;
import com.twilight.campus.service.StatisticsService;
import com.twilight.campus.vo.DashboardStatVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 数据统计控制器
 */
@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 后台首页统计数据
     */
    @GetMapping("/dashboard")
    public Result<DashboardStatVO> dashboard() {
        return Result.success(statisticsService.getDashboardStat());
    }
}
