package com.twilight.campus.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Content {
    private Long id;
    private String title;
    private String content;
    private String coverImg;
    private String mediaUrls;
    private Long categoryId;
    private Long activitySubCategoryId;
    private Long userId;
    private Integer status;
    private Integer isPrivate;
    private String auditReason;
    private Integer isTop;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount;
    private Integer commentCount;
    private BigDecimal price;
    private String tradeType;
    private String tradeCondition;
    private Integer tradeStatus;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String authorUsername;
    private String authorNickname;
    private String authorAvatar;
    private Long authorRoleId;
    private String categoryName;
    private String activitySubCategoryName;
}
