package com.twilight.campus.mapper;

import com.twilight.campus.pojo.UserBlock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserBlockMapper {
    int insert(UserBlock block);

    int deleteByBlockerAndBlocked(@Param("blockerId") Long blockerId, @Param("blockedUserId") Long blockedUserId);

    int countByBlockerAndBlocked(@Param("blockerId") Long blockerId, @Param("blockedUserId") Long blockedUserId);

    int countByUserId(@Param("userId") Long userId);
}
