package com.twilight.campus.vo;

import lombok.Data;

/**
 * 热门内容VO
 */
@Data
public class HotContentVO {

    private Long id;
    private String title;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
}
