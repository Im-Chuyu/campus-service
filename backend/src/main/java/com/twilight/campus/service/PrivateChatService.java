package com.twilight.campus.service;

import com.twilight.campus.dto.PrivateMessageSendDTO;
import com.twilight.campus.vo.PrivateChatPeerVO;
import com.twilight.campus.vo.PrivateChatSessionVO;
import com.twilight.campus.vo.PrivateMessageVO;

import java.util.List;

public interface PrivateChatService {
    PrivateChatPeerVO getPeer(Long peerId);

    List<PrivateChatSessionVO> sessions();

    List<PrivateMessageVO> history(Long peerId);

    List<PrivateMessageVO> sync(Long peerId, Long afterId);

    void send(PrivateMessageSendDTO dto);

    void block(Long peerId);

    void unblock(Long peerId);

    int unreadCount();

    int markAllRead();

    int markRead(Long peerId);
}
