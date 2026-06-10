package com.twilight.campus.dto;

import lombok.Data;

@Data
public class ActivitySubCategorySaveDTO {
    private Long id;
    private String name;
    private Integer sort;
    private Integer status;
}
