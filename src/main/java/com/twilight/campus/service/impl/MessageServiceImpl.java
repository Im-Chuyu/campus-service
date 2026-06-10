package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.MessageQueryDTO;
import com.twilight.campus.dto.MessageSendDTO;
import com.twilight.campus.dto.UserFeedbackDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.MessageMapper;
import com.twilight.campus.mapper.UserMapper;
import com.twilight.campus.pojo.Message;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.MessageService;
import com.twilight.campus.utils.AuthUtil;
import com.twilight.campus.utils.PageUtil;
import com.twilight.campus.vo.PageResultVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public void send(MessageSendDTO dto) {
        AuthUtil.checkAdmin();
        sendToUser(dto.getUserId(), dto.getTitle(), dto.getContent(), dto.getType(), dto.getRelatedId());
    }

    @Override
    public void submitFeedback(UserFeedbackDTO dto) {
        SysUser currentUser = AuthUtil.getLoginUser();
        List<SysUser> admins = userMapper.selectList(null, 1, 1L);
        if (admins == null || admins.isEmpty()) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "暂无管理员可接收反馈");
        }

        String displayName = currentUser.getNickname() != null && !currentUser.getNickname().trim().isEmpty()
                ? currentUser.getNickname()
                : currentUser.getUsername();
        String body = "用户" + displayName + "提交了平台反馈：" + dto.getContent().trim();
        for (SysUser admin : admins) {
            sendToUser(admin.getId(), "收到新的用户反馈", body, "SYSTEM", null);
        }
    }

    @Override
    public void sendToUser(Long userId, String title, String content, String type, Long relatedId) {
        if (userId == null) {
            return;
        }

        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setType(type);
        message.setIsRead(0);
        message.setRelatedId(relatedId);
        messageMapper.insert(message);
    }

    @Override
    public List<Message> myList() {
        SysUser currentUser = AuthUtil.getLoginUser();
        return messageMapper.selectByUserId(currentUser.getId());
    }

    @Override
    public int unreadCount() {
        SysUser currentUser = AuthUtil.getLoginUser();
        return messageMapper.countUnreadByUserId(currentUser.getId());
    }

    @Override
    public int markAllRead() {
        SysUser currentUser = AuthUtil.getLoginUser();
        return messageMapper.markAllReadByUserId(currentUser.getId());
    }

    @Override
    public void markRead(Long id) {
        SysUser currentUser = AuthUtil.getLoginUser();

        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "消息不存在");
        }

        if (!message.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只能标记自己的消息");
        }

        if (message.getIsRead() != null && message.getIsRead() == 1) {
            return;
        }

        messageMapper.markRead(id);
    }

    @Override
    public void deleteById(Long id) {
        SysUser currentUser = AuthUtil.getLoginUser();

        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "消息不存在");
        }

        if (!message.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只能删除自己的消息");
        }

        messageMapper.deleteById(id);
    }

    @Override
    public void deleteByRelatedId(Long relatedId) {
        if (relatedId == null) {
            return;
        }
        messageMapper.deleteByRelatedId(relatedId);
    }

    @Override
    public void deleteByUserTypeAndRelatedId(Long userId, String type, Long relatedId) {
        if (userId == null || type == null || relatedId == null) {
            return;
        }
        messageMapper.deleteByUserTypeAndRelatedId(userId, type, relatedId);
    }

    @Override
    public int markReadByUserTypeAndRelatedId(Long userId, String type, Long relatedId) {
        if (userId == null || type == null || relatedId == null) {
            return 0;
        }
        return messageMapper.markReadByUserTypeAndRelatedId(userId, type, relatedId);
    }

    @Override
    public int markReadByUserAndType(Long userId, String type) {
        if (userId == null || type == null) {
            return 0;
        }
        return messageMapper.markReadByUserAndType(userId, type);
    }

    @Override
    public List<Message> list(MessageQueryDTO query) {
        AuthUtil.checkAdmin();
        return messageMapper.selectList(query);
    }

    @Override
    public PageResultVO<Message> page(MessageQueryDTO query) {
        AuthUtil.checkAdmin();
        int page = PageUtil.safePage(query.getPage());
        int pageSize = PageUtil.safePageSize(query.getPageSize());
        query.setPage(page);
        query.setPageSize(pageSize);
        query.setOffset(PageUtil.offset(page, pageSize));
        Long total = messageMapper.countList(query);
        return PageResultVO.of(messageMapper.selectPage(query), total == null ? 0L : total, page, pageSize);
    }
}
