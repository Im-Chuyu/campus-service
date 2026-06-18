package com.twilight.campus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 点赞DTO
 */
@Data
public class LikeAddDTO {

    @NotNull(message = "内容ID不能为空")
    private Long contentId;
}
