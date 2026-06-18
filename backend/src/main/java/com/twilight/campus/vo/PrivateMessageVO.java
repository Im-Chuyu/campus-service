package com.twilight.campus.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrivateMessageVO {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Integer isRead;
    private LocalDateTime createTime;
    private String senderNickname;
    private String senderAvatar;
    private Long senderRoleId;
}
