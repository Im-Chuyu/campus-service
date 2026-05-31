package com.twilight.campus.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notice {
    private Long id;
    private String title;
    private String content;
    private Integer isTop;
    private Integer status;
    private Long publisherId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
