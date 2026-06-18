package com.twilight.campus.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFriend {
    private Long id;
    private Long userId;
    private Long friendId;
    private Long categoryId;
    private LocalDateTime createTime;
}
