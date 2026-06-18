package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发送消息DTO
 */
@Data
public class MessageSendDTO {

    @NotNull(message = "接收用户ID不能为空")
    private Long userId;

    @NotBlank(message = "消息标题不能为空")
    private String title;

    @NotBlank(message = "消息内容不能为空")
    private String content;

    @NotBlank(message = "消息类型不能为空")
    private String type;

    private Long relatedId;
}
