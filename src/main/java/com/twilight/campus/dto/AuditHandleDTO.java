package com.twilight.campus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审核处理DTO
 */
@Data
public class AuditHandleDTO {

    @NotNull(message = "内容ID不能为空")
    private Long contentId;

    @NotNull(message = "审核结果不能为空")
    private Integer auditResult; // 1通过，2拒绝

    private String auditReason;
}
