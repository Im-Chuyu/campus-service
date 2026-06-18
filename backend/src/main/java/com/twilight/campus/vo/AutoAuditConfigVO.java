package com.twilight.campus.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AutoAuditConfigVO {
    private Boolean enabled;
    private Boolean canUpdate;
}
