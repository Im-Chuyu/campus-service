package com.twilight.campus.dto;

import lombok.Data;

/**
 * 内容查询DTO
 */
@Data
public class ContentQueryDTO {

    private String title;
    private Long categoryId;
    private Integer status;
}
