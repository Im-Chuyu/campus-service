package com.twilight.campus.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AutoAuditConfigDTO {
    @NotNull(message = "自动审核状态不能为空")
    private Boolean enabled;
}
