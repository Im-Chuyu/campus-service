package com.twilight.campus.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrivateChatSessionVO {
    private Long peerId;
    private String username;
    private String nickname;
    private String avatar;
    private Long roleId;
    private String lastContent;
    private LocalDateTime lastTime;
    private Integer unreadCount;
}
