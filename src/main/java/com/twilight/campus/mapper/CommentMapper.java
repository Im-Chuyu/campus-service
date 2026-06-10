package com.twilight.campus.mapper;

import com.twilight.campus.pojo.Comment;
import com.twilight.campus.vo.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {

    /**
     * 根据内容ID查询评论列表
     * 这里只查正常状态的评论
     */
    List<CommentVO> selectByContentId(Long contentId);

    List<Comment> selectAllByContentId(Long contentId);

    List<Comment> selectAllByUserId(Long userId);

    Long countRootByContentId(Long contentId);

    List<CommentVO> selectRootPageByContentId(@Param("contentId") Long contentId,
                                              @Param("offset") Integer offset,
                                              @Param("pageSize") Integer pageSize);

    List<CommentVO> selectRepliesByParentIds(@Param("parentIds") List<Long> parentIds);

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

    int updateTopStatus(Comment comment);
}
