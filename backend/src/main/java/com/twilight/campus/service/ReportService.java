package com.twilight.campus.service;

import com.twilight.campus.dto.ReportAddDTO;
import com.twilight.campus.dto.ReportHandleDTO;
import com.twilight.campus.dto.ReportQueryDTO;
import com.twilight.campus.pojo.Report;
import com.twilight.campus.vo.PageResultVO;

import java.util.List;

public interface ReportService {

    /**
     * 提交举报
     */
    void add(ReportAddDTO dto);

    /**
     * 查询我的举报
     */
    List<Report> myList();

    /**
     * 查询待处理举报列表
     */
    List<Report> pendingList();

    /**
     * 处理举报
     */
    void handle(ReportHandleDTO dto);

    /**
     * 查询举报详情
     */
    Report getById(Long id);

    /**
     * 条件查询举报列表
     */
    List<Report> list(ReportQueryDTO query);

    PageResultVO<Report> page(ReportQueryDTO query);
}
