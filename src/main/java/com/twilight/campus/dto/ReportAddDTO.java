package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 举报提交DTO
 */
@Data
public class ReportAddDTO {

    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    @NotBlank(message = "举报类型不能为空")
    private String reportType;

    @NotBlank(message = "举报原因不能为空")
    private String reportReason;
}
