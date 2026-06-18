package com.twilight.campus.vo;

import lombok.Data;

import java.util.List;

@Data
public class PageResultVO<T> {

    private List<T> records;
    private Long total;
    private Integer page;
    private Integer pageSize;
    private Boolean hasMore;

    public static <T> PageResultVO<T> of(List<T> records, Long total, Integer page, Integer pageSize) {
        PageResultVO<T> result = new PageResultVO<>();
        result.setRecords(records);
        result.setTotal(total);
        result.setPage(page);
        result.setPageSize(pageSize);
        result.setHasMore((long) page * pageSize < total);
        return result;
    }
}
