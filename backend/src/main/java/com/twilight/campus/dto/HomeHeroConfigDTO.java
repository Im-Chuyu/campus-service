package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class HomeHeroConfigDTO {
    @NotBlank(message = "首页大卡片标题不能为空")
    private String title;

    @NotBlank(message = "首页大卡片内容不能为空")
    private String content;

    private String background;
}
