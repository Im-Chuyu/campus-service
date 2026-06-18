package com.twilight.campus.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FavoriteVO {

    private Long id;
    private Long contentId;

    private String title;
    private String content;
    private String coverImg;
    private Long categoryId;

    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private Integer viewCount;
    private Integer status;

    private LocalDateTime createTime;
}
