package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.PrivateMessageSendDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.PrivateMessageMapper;
import com.twilight.campus.mapper.UserBlockMapper;
import com.twilight.campus.mapper.UserMapper;
import com.twilight.campus.pojo.PrivateMessage;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.pojo.UserBlock;
import com.twilight.campus.service.MessageService;
import com.twilight.campus.service.PrivateChatService;
import com.twilight.campus.utils.AuthUtil;
import com.twilight.campus.vo.PrivateChatPeerVO;
import com.twilight.campus.vo.PrivateChatSessionVO;
import com.twilight.campus.vo.PrivateMessageVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class PrivateChatServiceImpl implements PrivateChatService {

    @Autowired
    private PrivateMessageMapper privateMessageMapper;

    @Autowired
    private UserBlockMapper userBlockMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MessageService messageService;

    @Override
    public PrivateChatPeerVO getPeer(Long peerId) {
        SysUser currentUser = requireLogin();
        if (peerId == null || peerId.equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "不能查看自己的私聊对象");
        }

        SysUser peer = requirePeer(peerId);
        PrivateChatPeerVO vo = new PrivateChatPeerVO();
        BeanUtils.copyProperties(maskForViewer(peer, false), vo);

        boolean blockedByMe = userBlockMapper.countByBlockerAndBlocked(currentUser.getId(), peerId) > 0;
        boolean blockedMe = userBlockMapper.countByBlockerAndBlocked(peerId, currentUser.getId()) > 0;
        vo.setBlockedByMe(blockedByMe ? 1 : 0);
        vo.setBlockedMe(blockedMe ? 1 : 0);
        vo.setCanChat(calculateCanChat(peer, blockedByMe, blockedMe) ? 1 : 0);
        return vo;
    }

    @Override
    public List<PrivateChatSessionVO> sessions() {
        SysUser currentUser = requireLogin();
        return privateMessageMapper.selectSessions(currentUser.getId());
    }

    @Override
    public List<PrivateMessageVO> history(Long peerId) {
        SysUser currentUser = requireLogin();
        if (peerId == null || peerId.equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "不能查看自己的私聊记录");
        }

        requirePeer(peerId);
        List<PrivateMessageVO> list = privateMessageMapper.selectConversation(currentUser.getId(), peerId);
        markConversationRead(currentUser.getId(), peerId);
        return list;
    }

    @Override
    public List<PrivateMessageVO> sync(Long peerId, Long afterId) {
        SysUser currentUser = requireLogin();
        if (peerId == null || peerId.equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "不能同步自己的私聊记录");
        }

        requirePeer(peerId);
        Long safeAfterId = afterId == null || afterId < 0 ? 0L : afterId;
        List<PrivateMessageVO> list = privateMessageMapper.selectConversationAfterId(currentUser.getId(), peerId, safeAfterId);
        if (!list.isEmpty()) {
            markConversationRead(currentUser.getId(), peerId);
        }
        return list;
    }

    @Override
    public void send(PrivateMessageSendDTO dto) {
        SysUser currentUser = requireLogin();
        if (dto.getReceiverId() == null || dto.getReceiverId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "接收者不正确");
        }

        SysUser receiver = requirePeer(dto.getReceiverId());
        ensureCanChat(currentUser.getId(), receiver);

        PrivateMessage message = new PrivateMessage();
        message.setSenderId(currentUser.getId());
        message.setReceiverId(dto.getReceiverId());
        message.setContent(dto.getContent().trim());
        message.setIsRead(0);
        privateMessageMapper.insert(message);
        privateMessageMapper.deleteConversationOverflow(currentUser.getId(), dto.getReceiverId());

        String senderName = currentUser.getNickname() != null && !currentUser.getNickname().trim().isEmpty()
                ? currentUser.getNickname()
                : currentUser.getUsername();
        messageService.deleteByUserTypeAndRelatedId(dto.getReceiverId(), "PRIVATE_CHAT", currentUser.getId());
        messageService.sendToUser(
                dto.getReceiverId(),
                "收到新的私聊消息",
                senderName + "给你发来一条私聊消息：" + message.getContent(),
                "PRIVATE_CHAT",
                currentUser.getId()
        );
    }

    @Override
    public void block(Long peerId) {
        SysUser currentUser = requireLogin();
        if (peerId == null || peerId.equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "不能屏蔽自己");
        }
        requirePeer(peerId);
        if (userBlockMapper.countByBlockerAndBlocked(currentUser.getId(), peerId) > 0) {
            return;
        }
        UserBlock block = new UserBlock();
        block.setBlockerId(currentUser.getId());
        block.setBlockedUserId(peerId);
        userBlockMapper.insert(block);
    }

    @Override
    public void unblock(Long peerId) {
        SysUser currentUser = requireLogin();
        if (peerId == null || peerId.equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "不能取消屏蔽自己");
        }
        userBlockMapper.deleteByBlockerAndBlocked(currentUser.getId(), peerId);
    }

    @Override
    public int unreadCount() {
        SysUser currentUser = requireLogin();
        return privateMessageMapper.countUnreadByUserId(currentUser.getId());
    }

    @Override
    public int markAllRead() {
        SysUser currentUser = requireLogin();
        int updated = privateMessageMapper.markAllReadByUserId(currentUser.getId());
        messageService.markReadByUserAndType(currentUser.getId(), "PRIVATE_CHAT");
        return updated;
    }

    @Override
    public int markRead(Long peerId) {
        SysUser currentUser = requireLogin();
        if (peerId == null || peerId.equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "私聊对象不正确");
        }
        requirePeer(peerId);
        return markConversationRead(currentUser.getId(), peerId);
    }

    private int markConversationRead(Long userId, Long peerId) {
        int updated = privateMessageMapper.markConversationRead(userId, peerId);
        messageService.markReadByUserTypeAndRelatedId(userId, "PRIVATE_CHAT", peerId);
        return updated;
    }

    private SysUser requireLogin() {
        SysUser currentUser = AuthUtil.getLoginUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }
        return currentUser;
    }

    private SysUser requirePeer(Long peerId) {
        SysUser peer = userMapper.selectById(peerId);
        if (peer == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }
        return peer;
    }

    private void ensureCanChat(Long currentUserId, SysUser receiver) {
        boolean blockedByMe = userBlockMapper.countByBlockerAndBlocked(currentUserId, receiver.getId()) > 0;
        boolean blockedMe = userBlockMapper.countByBlockerAndBlocked(receiver.getId(), currentUserId) > 0;
        if (blockedByMe || blockedMe) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "当前双方存在屏蔽关系，无法私聊");
        }
        if (receiver.getStatus() == null || receiver.getStatus() != 1) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "对方账号不可用");
        }
        if (receiver.getPrivateChatEnabled() == null || receiver.getPrivateChatEnabled() != 1) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "对方已关闭私聊");
        }
    }

    private boolean calculateCanChat(SysUser peer, boolean blockedByMe, boolean blockedMe) {
        return peer.getStatus() != null
            && peer.getStatus() == 1
            && peer.getPrivateChatEnabled() != null
            && peer.getPrivateChatEnabled() == 1
            && !blockedByMe
            && !blockedMe;
    }

    private SysUser maskForViewer(SysUser user, boolean isSelf) {
        if (isSelf) {
            return user;
        }

        SysUser masked = new SysUser();
        BeanUtils.copyProperties(user, masked);
        masked.setPhone(user.getPhoneVisible() != null && user.getPhoneVisible() == 1 ? user.getPhone() : null);
        masked.setEmail(user.getEmailVisible() != null && user.getEmailVisible() == 1 ? user.getEmail() : null);
        masked.setWechat(user.getWechatVisible() != null && user.getWechatVisible() == 1 ? user.getWechat() : null);
        masked.setQq(user.getQqVisible() != null && user.getQqVisible() == 1 ? user.getQq() : null);
        return masked;
    }
}
