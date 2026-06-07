package com.twilight.campus.service;

import com.twilight.campus.dto.CommentAddDTO;
import com.twilight.campus.pojo.Comment;
import com.twilight.campus.vo.CommentVO;

import java.util.List;

public interface CommentService {

    /**
     * 发表评论
     */
    void add(CommentAddDTO dto);

    /**
     * 查询某个内容下的评论
     */
    List<CommentVO> listByContentId(Long contentId);

    /**
     * 删除评论
     */
    void deleteById(Long id);
}
