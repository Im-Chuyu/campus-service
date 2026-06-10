package com.twilight.campus.dto;

import lombok.Data;

/**
 * 消息查询DTO
 */
@Data
public class MessageQueryDTO {

    private Integer isRead;
    private String type;
    private Integer page;
    private Integer pageSize;
    private Integer offset;
}
