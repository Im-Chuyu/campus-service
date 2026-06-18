package com.twilight.campus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FriendCategoryAssignDTO {
    @NotNull(message = "好友ID不能为空")
    private Long friendId;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
}
