package com.twilight.campus.service;

import com.twilight.campus.dto.CommentAddDTO;
import com.twilight.campus.vo.CommentVO;
import com.twilight.campus.vo.PageResultVO;

import java.util.List;

public interface CommentService {

    void add(CommentAddDTO dto);

    List<CommentVO> listByContentId(Long contentId);

    PageResultVO<CommentVO> pageByContentId(Long contentId, Integer page, Integer pageSize);

    void deleteById(Long id);

    void react(Long commentId, Integer reactionType);

    void toggleTop(Long commentId);
}
