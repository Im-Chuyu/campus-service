package com.twilight.campus.mapper;

import com.twilight.campus.pojo.SysConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SysConfigMapper {
    SysConfig selectByKey(String configKey);

    int insert(SysConfig sysConfig);

    int updateByKey(SysConfig sysConfig);
}
