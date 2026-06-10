package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.ContentMapper;
import com.twilight.campus.mapper.LikeRecordMapper;
import com.twilight.campus.pojo.Content;
import com.twilight.campus.pojo.LikeRecord;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.LikeCounterBufferService;
import com.twilight.campus.service.LikeRecordService;
import com.twilight.campus.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 点赞业务实现类
 */
@Transactional
@Service
public class LikeRecordServiceImpl implements LikeRecordService {

    @Autowired
    private LikeRecordMapper likeRecordMapper;

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private LikeCounterBufferService likeCounterBufferService;

    @Override
    public void add(Long contentId) {
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }
        if (!canViewContent(content, currentUser)) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "无权限操作该私密内容");
        }

        LikeRecord exist = likeRecordMapper.selectByUserIdAndContentId(currentUser.getId(), contentId);
        if (exist != null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "你已经点过赞了");
        }

        LikeRecord likeRecord = new LikeRecord();
        likeRecord.setUserId(currentUser.getId());
        likeRecord.setContentId(contentId);
        likeRecordMapper.insert(likeRecord);

        if (!likeCounterBufferService.recordContentLikeDelta(contentId, 1)) {
            contentMapper.increaseLikeCount(contentId);
        }
    }

    @Override
    public void delete(Long contentId) {
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        LikeRecord exist = likeRecordMapper.selectByUserIdAndContentId(currentUser.getId(), contentId);
        if (exist == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "你还没有点赞这个内容");
        }

        likeRecordMapper.deleteByUserIdAndContentId(currentUser.getId(), contentId);

        if (!likeCounterBufferService.recordContentLikeDelta(contentId, -1)) {
            contentMapper.decreaseLikeCount(contentId);
        }
    }

    @Override
    public List<LikeRecord> myList() {
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }
        return likeRecordMapper.selectByUserId(currentUser.getId());
    }

    private boolean canViewContent(Content content, SysUser currentUser) {
        if (!Integer.valueOf(1).equals(content.getIsPrivate())) {
            return true;
        }
        boolean isAdmin = currentUser.getRoleId() != null && currentUser.getRoleId().equals(1L);
        return isAdmin || currentUser.getId().equals(content.getUserId());
    }
}
