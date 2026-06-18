package com.twilight.campus.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PrivateMessageSendDTO {

    @NotNull(message = "接收者不能为空")
    private Long receiverId;

    @NotNull(message = "消息内容不能为空")
    @Size(min = 1, max = 1000, message = "消息内容长度应在1到1000之间")
    private String content;
}
