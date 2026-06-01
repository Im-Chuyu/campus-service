package com.twilight.campus.service;

import com.twilight.campus.dto.MessageQueryDTO;
import com.twilight.campus.dto.MessageSendDTO;
import com.twilight.campus.pojo.Message;

import java.util.List;

public interface MessageService {

    /**
     * 发送消息
     */
    void send(MessageSendDTO dto);

    /**
     * 当前用户消息列表
     */
    List<Message> myList();

    /**
     * 当前用户未读消息数
     */
    int unreadCount();

    /**
     * 标记已读
     */
    void markRead(Long id);

    /**
     * 删除消息
     */
    void deleteById(Long id);

    /**
     * 条件查询消息（管理员可用）
     */
    List<Message> list(MessageQueryDTO query);
}
