package com.twilight.campus.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentReaction {
    private Long id;
    private Long commentId;
    private Long userId;
    private Integer reactionType;
    private LocalDateTime createTime;
}
