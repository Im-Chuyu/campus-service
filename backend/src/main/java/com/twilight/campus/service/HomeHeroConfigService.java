package com.twilight.campus.service;

import com.twilight.campus.dto.HomeHeroConfigDTO;
import com.twilight.campus.vo.HomeHeroConfigVO;

public interface HomeHeroConfigService {
    HomeHeroConfigVO getConfig();

    void updateConfig(HomeHeroConfigDTO dto);
}
