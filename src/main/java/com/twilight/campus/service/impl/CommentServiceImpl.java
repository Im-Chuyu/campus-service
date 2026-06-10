package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.CommentAddDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.CommentMapper;
import com.twilight.campus.mapper.CommentReactionMapper;
import com.twilight.campus.mapper.ContentMapper;
import com.twilight.campus.pojo.Comment;
import com.twilight.campus.pojo.CommentReaction;
import com.twilight.campus.pojo.Content;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.CommentService;
import com.twilight.campus.service.MessageService;
import com.twilight.campus.utils.UserContext;
import com.twilight.campus.vo.CommentReactionStatVO;
import com.twilight.campus.vo.CommentVO;
import com.twilight.campus.vo.PageResultVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;

@Transactional
@Service
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private CommentReactionMapper commentReactionMapper;

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private MessageService messageService;

    @Override
    public void add(CommentAddDTO dto) {
        SysUser currentUser = requireLogin();
        Content content = requireContent(dto.getContentId());
        validateCommentText(dto.getCommentText());

        Comment parentComment = null;
        if (dto.getParentId() != null) {
            parentComment = requireComment(dto.getParentId());
            validateReplyTarget(parentComment, dto.getContentId());
        }

        Comment comment = new Comment();
        BeanUtils.copyProperties(dto, comment);
        comment.setUserId(currentUser.getId());
        comment.setStatus(1);
        comment.setIsTop(0);
        commentMapper.insert(comment);

        contentMapper.increaseCommentCount(content.getId());

        sendCommentNotice(currentUser, content, parentComment, dto.getCommentText());
    }

    @Override
    public List<CommentVO> listByContentId(Long contentId) {
        requireContent(contentId);

        List<CommentVO> list = commentMapper.selectByContentId(contentId);
        fillReactionStats(list);

        return list;
    }

    @Override
    public PageResultVO<CommentVO> pageByContentId(Long contentId, Integer page, Integer pageSize) {
        requireContent(contentId);

        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 50);
        int offset = (safePage - 1) * safePageSize;

        Long total = commentMapper.countRootByContentId(contentId);
        List<CommentVO> rootList = commentMapper.selectRootPageByContentId(contentId, offset, safePageSize);
        if (rootList.isEmpty()) {
            return PageResultVO.of(rootList, total == null ? 0L : total, safePage, safePageSize);
        }

        List<CommentVO> pageList = new ArrayList<>(rootList);
        List<Long> rootIds = rootList.stream().map(CommentVO::getId).toList();
        if (!rootIds.isEmpty()) {
            pageList.addAll(commentMapper.selectRepliesByParentIds(rootIds));
        }

        fillReactionStats(pageList);
        return PageResultVO.of(pageList, total == null ? 0L : total, safePage, safePageSize);
    }

    private void fillReactionStats(List<CommentVO> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        Long currentUserId = getCurrentUserIdOrNull();
        Set<Long> commentIds = list.stream().map(CommentVO::getId).collect(Collectors.toSet());
        Map<Long, CommentReactionStatVO> statMap = commentReactionMapper
            .selectStatsByCommentIds(List.copyOf(commentIds), currentUserId)
            .stream()
            .collect(Collectors.toMap(CommentReactionStatVO::getCommentId, stat -> stat));

        for (CommentVO vo : list) {
            CommentReactionStatVO stat = statMap.get(vo.getId());
            vo.setLikeCount(stat == null || stat.getLikeCount() == null ? 0 : stat.getLikeCount());
            vo.setDislikeCount(stat == null || stat.getDislikeCount() == null ? 0 : stat.getDislikeCount());
            vo.setMyReactionType(stat == null ? 0 : safeInt(stat.getMyReactionType()));
        }
    }

    @Override
    public void deleteById(Long id) {
        SysUser currentUser = requireLogin();
        Comment comment = requireComment(id);

        boolean isOwner = comment.getUserId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRoleId() != null && currentUser.getRoleId().equals(1L);
        if (!isOwner && !isAdmin) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只能删除自己的评论");
        }

        Comment updateComment = new Comment();
        updateComment.setId(id);
        updateComment.setStatus(0);
        commentMapper.updateStatus(updateComment);

        contentMapper.decreaseCommentCount(comment.getContentId());
    }

    @Override
    public void react(Long commentId, Integer reactionType) {
        SysUser currentUser = requireLogin();
        Comment comment = requireComment(commentId);
        Content content = requireContent(comment.getContentId());

        if (reactionType == null || reactionType < 0 || reactionType > 2) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "无效的评论反应类型");
        }

        CommentReaction exist = commentReactionMapper.selectByUserIdAndCommentId(currentUser.getId(), commentId);
        Integer oldReactionType = exist == null ? 0 : exist.getReactionType();

        if (reactionType == 0) {
            if (exist != null) {
                commentReactionMapper.deleteByUserIdAndCommentId(currentUser.getId(), commentId);
            }
            return;
        }

        if (exist == null) {
            CommentReaction reaction = new CommentReaction();
            reaction.setCommentId(commentId);
            reaction.setUserId(currentUser.getId());
            reaction.setReactionType(reactionType);
            commentReactionMapper.insert(reaction);
        } else if (exist.getReactionType().equals(reactionType)) {
            commentReactionMapper.deleteByUserIdAndCommentId(currentUser.getId(), commentId);
            return;
        } else {
            exist.setReactionType(reactionType);
            commentReactionMapper.updateReactionType(exist);
        }

        if (reactionType == 1 && !comment.getUserId().equals(currentUser.getId())) {
            sendLikeNotice(currentUser, content, comment);
        }
    }

    @Override
    public void toggleTop(Long commentId) {
        SysUser currentUser = requireLogin();
        Comment comment = requireComment(commentId);
        Content content = requireContent(comment.getContentId());

        boolean isAdmin = currentUser.getRoleId() != null && currentUser.getRoleId().equals(1L);
        boolean isContentOwner = content.getUserId().equals(currentUser.getId());
        if (!isAdmin && !isContentOwner) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "No permission to pin this comment");
        }

        Comment updateComment = new Comment();
        updateComment.setId(commentId);
        updateComment.setIsTop(Integer.valueOf(1).equals(comment.getIsTop()) ? 0 : 1);
        commentMapper.updateTopStatus(updateComment);
    }

    private void sendCommentNotice(SysUser currentUser, Content content, Comment parentComment, String commentText) {
        String actorName = displayName(currentUser);
        String preview = buildPreview(commentText);

        if (parentComment == null) {
            if (!content.getUserId().equals(currentUser.getId())) {
                messageService.sendToUser(
                    content.getUserId(),
                    "你的帖子有新评论",
                    actorName + " 评论了你的帖子《" + content.getTitle() + "》：" + preview,
                    "COMMENT",
                    content.getId()
                );
            }
            return;
        }

        if (!parentComment.getUserId().equals(currentUser.getId())) {
            messageService.sendToUser(
                parentComment.getUserId(),
                "你的评论收到回复",
                actorName + " 回复了你在《" + content.getTitle() + "》下的评论：" + preview,
                "REPLY",
                content.getId()
            );
        }
    }

    private void sendLikeNotice(SysUser currentUser, Content content, Comment comment) {
        String actorName = displayName(currentUser);
        String preview = buildPreview(comment.getCommentText());
        messageService.sendToUser(
            comment.getUserId(),
            "你的评论被点赞了",
            actorName + " 点赞了你在《" + content.getTitle() + "》下的评论：" + preview,
            "LIKE",
            content.getId()
        );
    }

    private String displayName(SysUser user) {
        if (user.getNickname() != null && !user.getNickname().isBlank()) {
            return user.getNickname();
        }
        return user.getUsername();
    }

    private SysUser requireLogin() {
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }
        return currentUser;
    }

    private Long getCurrentUserIdOrNull() {
        SysUser currentUser = UserContext.getUser();
        return currentUser == null ? null : currentUser.getId();
    }

    private Content requireContent(Long contentId) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }
        if (!canViewContent(content)) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "无权限查看该私密内容");
        }
        return content;
    }

    private boolean canViewContent(Content content) {
        if (!Integer.valueOf(1).equals(content.getIsPrivate())) {
            return true;
        }
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            return false;
        }
        boolean isAdmin = currentUser.getRoleId() != null && currentUser.getRoleId().equals(1L);
        return isAdmin || currentUser.getId().equals(content.getUserId());
    }

    private Comment requireComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null || comment.getStatus() == 0) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "评论不存在");
        }
        return comment;
    }

    private void validateCommentText(String commentText) {
        if (commentText == null || commentText.isBlank()) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "评论内容不能为空");
        }
        if (commentText.length() > 500) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "评论内容不能超过500字");
        }
    }

    private void validateReplyTarget(Comment parentComment, Long contentId) {
        if (!parentComment.getContentId().equals(contentId)) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "回复评论的内容不匹配");
        }
    }

    private String buildPreview(String commentText) {
        if (commentText == null) {
            return "";
        }
        String text = commentText.replaceAll("\\s+", " ").trim();
        if (text.length() <= 48) {
            return text;
        }
        return text.substring(0, 48) + "...";
    }

    private int safeInt(Integer value) {
        return value == null ? 0 : value;
    }
}
