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
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer myReactionType;
    private Integer isTop;

    private String username;
    private String nickname;
    private String avatar;
    private Long roleId;

    private LocalDateTime createTime;
}
