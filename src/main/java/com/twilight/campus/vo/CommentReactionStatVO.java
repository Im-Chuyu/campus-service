package com.twilight.campus.vo;

import lombok.Data;

@Data
public class CommentReactionStatVO {
    private Long commentId;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer myReactionType;
}
