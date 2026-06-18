package com.twilight.campus.mapper;

import com.twilight.campus.vo.HotContentVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StatisticsMapper {

    Long countUsers();

    Long countContents();

    Long countCategories();

    Long countTodayContents();

    Long countAuditedContents();

    Long countPassedContents();

    List<HotContentVO> selectHotContentList();
}
