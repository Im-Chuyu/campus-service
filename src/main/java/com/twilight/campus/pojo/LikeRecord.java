package com.twilight.campus.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LikeRecord {
    private Long id;
    private Long userId;
    private Long contentId;
    private LocalDateTime createTime;
}
