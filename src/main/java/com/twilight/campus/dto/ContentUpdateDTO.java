package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 修改内容DTO
 */
@Data
public class ContentUpdateDTO {

    @NotNull(message = "内容ID不能为空")
    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String coverImg;

    @NotNull(message = "分类不能为空")
    private Long categoryId;
}
