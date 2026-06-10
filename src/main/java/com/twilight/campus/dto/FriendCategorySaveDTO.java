package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FriendCategorySaveDTO {
    private Long id;

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 20, message = "分类名称不能超过20个字符")
    private String name;
}
