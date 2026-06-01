package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.MessageQueryDTO;
import com.twilight.campus.dto.MessageSendDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.MessageMapper;
import com.twilight.campus.pojo.Message;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.MessageService;
import com.twilight.campus.utils.AuthUtil;
import com.twilight.campus.utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 消息业务实现类
 */
@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageMapper messageMapper;

    @Override
    public void send(MessageSendDTO dto) {
        // 发送消息一般由系统/管理员触发
        AuthUtil.checkAdmin();

        Message message = new Message();
        BeanUtils.copyProperties(dto, message);
        message.setIsRead(0);

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
    public List<Message> list(MessageQueryDTO query) {
        AuthUtil.checkAdmin();
        return messageMapper.selectList(query);
    }
}
