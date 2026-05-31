package com.twilight.campus.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    private Long id;
    private String title;
    private String content;
    private String coverImg;
    private Long categoryId;
    private Long userId;
    private Integer status;
    private String auditReason;
    private Integer isTop;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
