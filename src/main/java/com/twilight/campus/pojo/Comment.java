package com.twilight.campus.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Long id;
    private Long contentId;
    private Long userId;
    private Long parentId;
    private String commentText;
    private Integer status;
    private LocalDateTime createTime;
}
