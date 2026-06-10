package com.twilight.campus.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendVO {
    private Long friendId;
    private Long categoryId;
    private String categoryName;
    private String username;
    private String nickname;
    private String signature;
    private String avatar;
    private String profileBackground;
    private Long roleId;
    private Integer privateChatEnabled;
    private Integer status;
    private Integer isFriend;
    private LocalDateTime createTime;
}
