package com.twilight.campus.dto;

import lombok.Data;

/**
 * 分类新增/修改DTO
 */
@Data
public class CategorySaveDTO {
    private Long id;
    private String name;
    private Integer sort;
    private Integer status;
}
