package com.twilight.campus.mapper;

import com.twilight.campus.pojo.LikeRecord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LikeRecordMapper {

    /**
     * 根据用户ID和内容ID查询是否已点赞
     */
    LikeRecord selectByUserIdAndContentId(Long userId, Long contentId);

    /**
     * 新增点赞
     */
    int insert(LikeRecord likeRecord);

    /**
     * 删除点赞
     */
    int deleteByUserIdAndContentId(Long userId, Long contentId);

    /**
     * 查询我的点赞记录
     */
    List<LikeRecord> selectByUserId(Long userId);
}
