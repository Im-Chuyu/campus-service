package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.CommentAddDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.CommentMapper;
import com.twilight.campus.mapper.ContentMapper;
import com.twilight.campus.pojo.Comment;
import com.twilight.campus.pojo.Content;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.CommentService;
import com.twilight.campus.utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 评论业务实现类
 */
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private ContentMapper contentMapper;

    @Override
    public void add(CommentAddDTO dto) {
        // 1. 检查当前用户是否登录
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        // 2. 检查内容是否存在
        Content content = contentMapper.selectById(dto.getContentId());
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }

        // 3. 检查评论内容长度
        if (dto.getCommentText().length() > 500) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "评论内容不能超过500字");
        }

        // 4. 如果传了父评论ID，检查父评论是否存在
        if (dto.getParentId() != null) {
            Comment parentComment = commentMapper.selectById(dto.getParentId());
            if (parentComment == null || parentComment.getStatus() == 0) {
                throw new BusinessException(ResultCodeConstant.NOT_FOUND, "父评论不存在");
            }

            // 父评论必须属于同一个内容，避免跨内容回复
            if (!parentComment.getContentId().equals(dto.getContentId())) {
                throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "回复评论的内容不匹配");
            }
        }

        // 5. 组装评论对象
        Comment comment = new Comment();
        BeanUtils.copyProperties(dto, comment);
        comment.setUserId(currentUser.getId());
        comment.setStatus(1);

        commentMapper.insert(comment);

        // 6. 内容评论数 +1
        // 这里我建议你后面在 ContentMapper 里补一个 updateCommentCount 方法
        content.setCommentCount(content.getCommentCount() == null ? 1 : content.getCommentCount() + 1);
        contentMapper.updateCommentCount(content);
    }

    @Override
    public List<Comment> listByContentId(Long contentId) {
        // 1. 检查内容是否存在
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }

        return commentMapper.selectByContentId(contentId);
    }

    @Override
    public void deleteById(Long id) {
        // 1. 当前用户
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        // 2. 查评论是否存在
        Comment comment = commentMapper.selectById(id);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "评论不存在");
        }

        // 3. 只能删除自己的评论，管理员也可以删除
        boolean isOwner = comment.getUserId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoleId() != null && currentUser.getRoleId().equals(1L);

        if (!isOwner && !isAdmin) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只能删除自己的评论");
        }

        // 4. 逻辑删除：把状态改为 0
        Comment updateComment = new Comment();
        updateComment.setId(id);
        updateComment.setStatus(0);
        commentMapper.updateStatus(updateComment);

        // 5. 评论数 -1
        Content content = contentMapper.selectById(comment.getContentId());
        if (content != null && content.getCommentCount() != null && content.getCommentCount() > 0) {
            content.setCommentCount(content.getCommentCount() - 1);
            contentMapper.updateCommentCount(content);
        }
    }
}
