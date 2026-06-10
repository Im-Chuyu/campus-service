package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 公告新增/修改DTO
 */
@Data
public class NoticeSaveDTO {

    private Long id;

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String heroBackground;

    /**
     * 是否发布：1个发布 0个草稿
     */
    @NotNull(message = "状态不能为空")
    private Integer status;

    /**
     * 是否置顶：1是 0否
     */
    @NotNull(message = "置顶状态不能为空")
    private Integer isTop;
}
