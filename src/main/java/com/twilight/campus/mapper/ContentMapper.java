package com.twilight.campus.mapper;

import com.twilight.campus.dto.ContentQueryDTO;
import com.twilight.campus.pojo.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface ContentMapper {

    /**
     * 条件查询内容列表
     */
    List<Content> selectList(ContentQueryDTO query);

    Long countList(ContentQueryDTO query);

    List<Content> selectPage(ContentQueryDTO query);

    /**
     * 根据ID查询内容
     */
    Content selectById(Long id);

    /**
     * 根据ID查询内容所属分类下有多少内容
     * 这个方法后面删除分类时也能复用
     */
    int countByCategoryId(Long categoryId);

    /**
     * 根据ID查询用户发布的内容数
     */
    int countByUserId(Long userId);

    /**
     * 新增内容
     */
    int insert(Content content);

    /**
     * 修改内容
     */
    int update(Content content);

    /**
     * 更新内容置顶状态
     */
    int updateTopStatus(@Param("id") Long id, @Param("isTop") Integer isTop);

    /**
     * 删除内容
     */
    int deleteById(Long id);

    /**
     * 查询某个用户发布的内容列表
     */
    List<Content> selectByUserId(Long userId);

    /**
     * 查询待审核内容列表
     */
    List<Content> selectWaitAuditList();

    Long countWaitAuditList();

    List<Content> selectWaitAuditPage(@Param("offset") Integer offset, @Param("pageSize") Integer pageSize);

    /**
     * 更新内容审核状态和原因
     */
    int updateAuditStatus(Content content);

    /**
     * 管理员将内容打回待审核状态
     */
    int returnToAudit(Content content);

    /**
     * 更新内容评论数
     */
    int updateCommentCount(Content content);

    int increaseCommentCount(Long id);

    int decreaseCommentCount(Long id);

    /**
     * 更新点赞收藏
     */
    int increaseLikeCount(Long id);

    int decreaseLikeCount(Long id);

    int updateLikeCountByDelta(@Param("id") Long id, @Param("delta") Integer delta);

    int updateViewCountByDelta(@Param("id") Long id, @Param("delta") Integer delta);

    int increaseFavoriteCount(Long id);

    int decreaseFavoriteCount(Long id);

    int increaseViewCount(Long id);

    int refreshInteractionCounts(@Param("ids") Set<Long> ids);

}
