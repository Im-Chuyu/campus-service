package com.twilight.campus.dto;

import lombok.Data;

/**
 * 举报查询DTO
 */
@Data
public class ReportQueryDTO {

    private Integer status;
    private Long contentId;
    private Integer page;
    private Integer pageSize;
    private Integer offset;
}
