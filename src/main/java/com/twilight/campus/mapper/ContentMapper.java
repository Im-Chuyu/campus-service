package com.twilight.campus.mapper;

import com.twilight.campus.dto.ContentQueryDTO;
import com.twilight.campus.pojo.Content;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ContentMapper {

    /**
     * 条件查询内容列表
     */
    List<Content> selectList(ContentQueryDTO query);

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

    /**
     * 更新内容审核状态和原因
     */
    int updateAuditStatus(Content content);
}
