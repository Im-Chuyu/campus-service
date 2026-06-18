package com.twilight.campus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 收藏DTO
 */
@Data
public class FavoriteAddDTO {

    @NotNull(message = "内容ID不能为空")
    private Long contentId;
}
