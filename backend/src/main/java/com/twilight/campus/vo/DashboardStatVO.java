package com.twilight.campus.vo;

import lombok.Data;

import java.util.List;

/**
 * 首页统计VO
 */
@Data
public class DashboardStatVO {

    private Long userTotal;

    private Long contentTotal;

    private Long categoryTotal;

    private Long todayContentTotal;

    private Long auditedContentTotal;

    private Long auditPassRate;

    private List<HotContentVO> hotContentList;
}
