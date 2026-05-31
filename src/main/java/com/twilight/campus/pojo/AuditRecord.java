package com.twilight.campus.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditRecord {
    private Long id;
    private Long contentId;
    private Long auditorId;
    private Integer auditResult;
    private String auditReason;
    private LocalDateTime createTime;
}
