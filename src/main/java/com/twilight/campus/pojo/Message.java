package com.twilight.campus.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String type;
    private Integer isRead;
    private Long relatedId;
    private LocalDateTime createTime;
}
