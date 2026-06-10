package com.twilight.campus.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 内容查询DTO
 */
@Data
public class ContentQueryDTO {

    private String title;
    private Long categoryId;
    private Long activitySubCategoryId;
    private Integer status;
    private Long userId;
    private String tradeType;
    private Integer tradeStatus;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private Boolean includePrivate;
    private String sort;
    private Integer page;
    private Integer pageSize;
    private Integer offset;
}
