package com.twilight.campus.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserBlock {
    private Long id;
    private Long blockerId;
    private Long blockedUserId;
    private LocalDateTime createTime;
}
