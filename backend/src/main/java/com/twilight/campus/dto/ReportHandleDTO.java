package com.twilight.campus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 举报处理DTO
 */
@Data
public class ReportHandleDTO {

    @NotNull(message = "举报ID不能为空")
    private Long reportId;

    @NotBlank(message = "处理结果不能为空")
    private String handleResult;
}
