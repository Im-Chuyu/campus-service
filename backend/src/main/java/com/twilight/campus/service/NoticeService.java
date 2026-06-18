package com.twilight.campus.service;

import com.twilight.campus.dto.NoticeQueryDTO;
import com.twilight.campus.dto.NoticeSaveDTO;
import com.twilight.campus.pojo.Notice;
import com.twilight.campus.vo.PageResultVO;

import java.util.List;

public interface NoticeService {

    /**
     * 公告列表
     */
    List<Notice> list(NoticeQueryDTO query);

    PageResultVO<Notice> page(NoticeQueryDTO query);

    /**
     * 已发布公告列表
     */
    List<Notice> publishedList();

    /**
     * 公告详情
     */
    Notice getById(Long id);

    /**
     * 新增公告
     */
    void add(NoticeSaveDTO dto);

    /**
     * 修改公告
     */
    void update(NoticeSaveDTO dto);

    /**
     * 删除公告
     */
    void deleteById(Long id);

    /**
     * 切换置顶状态
     */
    void toggleTop(Long id);
}
