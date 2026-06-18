package com.twilight.campus.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.HomeHeroConfigDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.SysConfigMapper;
import com.twilight.campus.pojo.SysConfig;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.HomeHeroConfigService;
import com.twilight.campus.utils.AuthUtil;
import com.twilight.campus.utils.UserContext;
import com.twilight.campus.vo.HomeHeroConfigVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HomeHeroConfigServiceImpl implements HomeHeroConfigService {

    private static final String HOME_HERO_KEY = "HOME_HERO_CONFIG";
    private static final String HOME_HERO_DESCRIPTION = "首页大卡片展示配置";
    private static final String DEFAULT_TITLE = "今天的校园动态，都从这里开始";
    private static final String DEFAULT_CONTENT = "发布校园信息、查看活动通知、寻找失物线索，让校园服务更集中也更高效。";
    private static final long CACHE_TTL_MILLIS = 5 * 60 * 1000L;

    private final Object cacheLock = new Object();
    private volatile HomeHeroConfigVO cachedConfig;
    private volatile long cachedAtMillis;

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public HomeHeroConfigVO getConfig() {
        HomeHeroConfigVO config = getCachedConfig();
        config.setCanUpdate(AuthUtil.isSuperAdmin(UserContext.getUser()));
        return config;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateConfig(HomeHeroConfigDTO dto) {
        SysUser currentUser = UserContext.getUser();
        if (!AuthUtil.isSuperAdmin(currentUser)) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只有超级管理员admin可以修改首页大卡片");
        }

        HomeHeroConfigVO config = new HomeHeroConfigVO(
                dto.getTitle().trim(),
                dto.getContent().trim(),
                dto.getBackground() == null ? "" : dto.getBackground().trim(),
                true
        );

        SysConfig sysConfig = new SysConfig();
        sysConfig.setConfigKey(HOME_HERO_KEY);
        sysConfig.setConfigValue(toJson(config));
        sysConfig.setDescription(HOME_HERO_DESCRIPTION);

        if (sysConfigMapper.selectByKey(HOME_HERO_KEY) == null) {
            sysConfigMapper.insert(sysConfig);
        } else {
            sysConfigMapper.updateByKey(sysConfig);
        }

        refreshCache(config);
    }

    private HomeHeroConfigVO getCachedConfig() {
        long now = System.currentTimeMillis();
        HomeHeroConfigVO current = cachedConfig;
        if (current != null && now - cachedAtMillis < CACHE_TTL_MILLIS) {
            return copyConfig(current);
        }

        synchronized (cacheLock) {
            now = System.currentTimeMillis();
            current = cachedConfig;
            if (current != null && now - cachedAtMillis < CACHE_TTL_MILLIS) {
                return copyConfig(current);
            }

            HomeHeroConfigVO loaded = readConfig();
            cachedConfig = copyConfig(loaded);
            cachedAtMillis = now;
            return copyConfig(loaded);
        }
    }

    private void refreshCache(HomeHeroConfigVO config) {
        synchronized (cacheLock) {
            cachedConfig = copyConfig(config);
            cachedAtMillis = System.currentTimeMillis();
        }
    }

    private HomeHeroConfigVO readConfig() {
        SysConfig sysConfig = sysConfigMapper.selectByKey(HOME_HERO_KEY);
        if (sysConfig == null || sysConfig.getConfigValue() == null || sysConfig.getConfigValue().trim().isEmpty()) {
            return defaultConfig();
        }

        try {
            HomeHeroConfigVO config = objectMapper.readValue(sysConfig.getConfigValue(), HomeHeroConfigVO.class);
            if (config.getTitle() == null || config.getTitle().trim().isEmpty()) {
                config.setTitle(DEFAULT_TITLE);
            }
            if (config.getContent() == null || config.getContent().trim().isEmpty()) {
                config.setContent(DEFAULT_CONTENT);
            }
            if (config.getBackground() == null) {
                config.setBackground("");
            }
            return config;
        } catch (Exception e) {
            return defaultConfig();
        }
    }

    private HomeHeroConfigVO defaultConfig() {
        return new HomeHeroConfigVO(DEFAULT_TITLE, DEFAULT_CONTENT, "", false);
    }

    private HomeHeroConfigVO copyConfig(HomeHeroConfigVO config) {
        return new HomeHeroConfigVO(
                config.getTitle(),
                config.getContent(),
                config.getBackground(),
                config.getCanUpdate()
        );
    }

    private String toJson(HomeHeroConfigVO config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (Exception e) {
            throw new BusinessException(ResultCodeConstant.ERROR, "首页大卡片配置保存失败");
        }
    }
}
