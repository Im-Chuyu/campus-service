package com.twilight.campus.mapper;

import com.twilight.campus.pojo.Favorite;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FavoriteMapper {

    /**
     * 根据用户ID和内容ID查询是否已收藏
     */
    Favorite selectByUserIdAndContentId(Long userId, Long contentId);

    /**
     * 新增收藏
     */
    int insert(Favorite favorite);

    /**
     * 删除收藏
     */
    int deleteByUserIdAndContentId(Long userId, Long contentId);

    /**
     * 查询我的收藏
     */
    List<Favorite> selectByUserId(Long userId);
}
