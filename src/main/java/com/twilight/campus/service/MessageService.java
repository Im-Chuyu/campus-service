package com.twilight.campus.service;

import com.twilight.campus.dto.MessageQueryDTO;
import com.twilight.campus.dto.MessageSendDTO;
import com.twilight.campus.dto.UserFeedbackDTO;
import com.twilight.campus.pojo.Message;
import com.twilight.campus.vo.PageResultVO;

import java.util.List;

public interface MessageService {

    void send(MessageSendDTO dto);

    void submitFeedback(UserFeedbackDTO dto);

    void sendToUser(Long userId, String title, String content, String type, Long relatedId);

    List<Message> myList();

    int unreadCount();

    int markAllRead();

    void markRead(Long id);

    void deleteById(Long id);

    void deleteByRelatedId(Long relatedId);

    void deleteByUserTypeAndRelatedId(Long userId, String type, Long relatedId);

    int markReadByUserTypeAndRelatedId(Long userId, String type, Long relatedId);

    int markReadByUserAndType(Long userId, String type);

    List<Message> list(MessageQueryDTO query);

    PageResultVO<Message> page(MessageQueryDTO query);
}
