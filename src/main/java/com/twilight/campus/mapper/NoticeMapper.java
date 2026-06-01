package com.twilight.campus.mapper;

import com.twilight.campus.dto.NoticeQueryDTO;
import com.twilight.campus.pojo.Notice;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeMapper {

    /**
     * 查询公告列表
     * 如果是管理员，可以查全部；前台一般只查已发布公告
     */
    List<Notice> selectList(NoticeQueryDTO query);

    /**
     * 查询已发布公告列表
     */
    List<Notice> selectPublishedList();

    /**
     * 根据ID查询公告
     */
    Notice selectById(Long id);

    /**
     * 新增公告
     */
    int insert(Notice notice);

    /**
     * 修改公告
     */
    int update(Notice notice);

    /**
     * 删除公告
     */
    int deleteById(Long id);
}
