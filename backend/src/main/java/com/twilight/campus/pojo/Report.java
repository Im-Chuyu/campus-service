package com.twilight.campus.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private Long id;
    private Long userId;
    private Long contentId;
    private String reportType;
    private String reportReason;
    private Integer status;
    private Long handlerId;
    private String handleResult;
    private LocalDateTime createTime;
    private LocalDateTime handleTime;
}
