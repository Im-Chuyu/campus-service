package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发表评论DTO
 */
@Data
public class CommentAddDTO {

    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    /**
     * 父评论ID，回复某条评论时传；普通评论可不传
     */
    private Long parentId;

    @NotBlank(message = "评论内容不能为空")
    private String commentText;
}
