package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ContentReturnAuditDTO {

    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    @NotBlank(message = "打回原因不能为空")
    private String reason;
}
