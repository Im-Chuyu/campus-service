package com.twilight.campus.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PrivateChatPeerVO {
    private Long id;
    private String username;
    private String nickname;
    private String signature;
    private String avatar;
    private String profileBackground;
    private String phone;
    private Integer phoneVisible;
    private String email;
    private Integer emailVisible;
    private String wechat;
    private Integer wechatVisible;
    private String qq;
    private Integer qqVisible;
    private Integer privateChatEnabled;
    private Integer gender;
    private Long roleId;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer blockedByMe;
    private Integer blockedMe;
    private Integer canChat;
}
