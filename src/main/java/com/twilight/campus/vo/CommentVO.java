package com.twilight.campus.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {

    private Long id;
    private Long contentId;
    private Long parentId;
    private Long userId;

    private String commentText;

    private String username;
    private String nickname;
    private String avatar;

    private LocalDateTime createTime;
}
