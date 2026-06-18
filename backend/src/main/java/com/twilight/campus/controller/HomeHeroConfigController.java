package com.twilight.campus.controller;

import com.twilight.campus.dto.HomeHeroConfigDTO;
import com.twilight.campus.result.Result;
import com.twilight.campus.service.HomeHeroConfigService;
import com.twilight.campus.vo.HomeHeroConfigVO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home/hero")
public class HomeHeroConfigController {

    @Autowired
    private HomeHeroConfigService homeHeroConfigService;

    @GetMapping("/config")
    public Result<HomeHeroConfigVO> getConfig() {
        return Result.success(homeHeroConfigService.getConfig());
    }

    @GetMapping("/public")
    public Result<HomeHeroConfigVO> getPublicConfig() {
        HomeHeroConfigVO config = homeHeroConfigService.getConfig();
        config.setCanUpdate(false);
        return Result.success(config);
    }

    @PutMapping("/config")
    public Result<?> updateConfig(@RequestBody @Valid HomeHeroConfigDTO dto) {
        homeHeroConfigService.updateConfig(dto);
        return Result.success("首页大卡片已更新", null);
    }
}
