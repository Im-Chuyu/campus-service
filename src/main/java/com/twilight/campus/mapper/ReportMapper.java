package com.twilight.campus.mapper;

import com.twilight.campus.dto.ReportQueryDTO;
import com.twilight.campus.pojo.Report;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportMapper {

    /**
     * 新增举报
     */
    int insert(Report report);

    /**
     * 根据ID查询举报
     */
    Report selectById(Long id);

    /**
     * 查询我的举报
     */
    List<Report> selectByUserId(Long userId);

    /**
     * 查询待处理举报列表
     */
    List<Report> selectPendingList();

    /**
     * 查询举报列表（可扩展条件）
     */
    List<Report> selectList(ReportQueryDTO query);

    /**
     * 处理举报
     */
    int updateHandle(Report report);
}
