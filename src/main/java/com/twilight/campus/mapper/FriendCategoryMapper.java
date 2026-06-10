package com.twilight.campus.mapper;

import com.twilight.campus.pojo.FriendCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FriendCategoryMapper {
    int insert(FriendCategory category);

    int insertIgnore(FriendCategory category);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int countByUserId(Long userId);

    int countByUserIdAndName(@Param("userId") Long userId, @Param("name") String name, @Param("excludeId") Long excludeId);

    FriendCategory selectByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    FriendCategory selectFirstByUserId(Long userId);

    List<FriendCategory> selectByUserId(Long userId);

    int update(FriendCategory category);
}
