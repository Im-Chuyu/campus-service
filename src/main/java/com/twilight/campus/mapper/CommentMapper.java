package com.twilight.campus.mapper;

import com.twilight.campus.pojo.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 根据内容ID查询评论列表
     * 这里只查正常状态的评论
     */
    List<Comment> selectByContentId(Long contentId);

    /**
     * 根据评论ID查询详情
     */
    Comment selectById(Long id);

    /**
     * 新增评论
     */
    int insert(Comment comment);

    /**
     * 更新评论状态（删除时用）
     */
    int updateStatus(Comment comment);
}
