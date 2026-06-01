package com.twilight.campus.dto;

import lombok.Data;

/**
 * 公告查询DTO
 */
@Data
public class NoticeQueryDTO {

    private String title;
    private Integer status;
    private Integer isTop;
}
