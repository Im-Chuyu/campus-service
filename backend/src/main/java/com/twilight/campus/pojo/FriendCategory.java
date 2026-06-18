package com.twilight.campus.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendCategory {
    private Long id;
    private Long userId;
    private String name;
    private Integer sort;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
