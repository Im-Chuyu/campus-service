package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 发布内容DTO
 */
@Data
public class ContentAddDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverImg;

    private String mediaUrls;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    private Long activitySubCategoryId;

    private Integer isPrivate;

    private BigDecimal price;

    private String tradeType;

    private String tradeCondition;

    private Integer tradeStatus;
}
