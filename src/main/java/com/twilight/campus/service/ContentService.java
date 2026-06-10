package com.twilight.campus.service;

import com.twilight.campus.dto.ContentAddDTO;
import com.twilight.campus.dto.ContentQueryDTO;
import com.twilight.campus.dto.ContentReturnAuditDTO;
import com.twilight.campus.dto.ContentUpdateDTO;
import com.twilight.campus.pojo.Content;
import com.twilight.campus.vo.PageResultVO;

import java.util.List;

public interface ContentService {

    /**
     * 内容列表
     */
    List<Content> list(ContentQueryDTO query);

    PageResultVO<Content> page(ContentQueryDTO query);

    /**
     * 内容详情
     */
    Content getById(Long id);

    /**
     * 发布内容
     */
    void add(ContentAddDTO dto);

    /**
     * 修改内容
     */
    void update(ContentUpdateDTO dto);

    /**
     * 删除内容
     */
    void deleteById(Long id);

    /**
     * 管理员切换内容置顶状态
     */
    void toggleTop(Long id);

    /**
     * 管理员将内容打回待审核并通知发布者修改
     */
    void returnToAudit(ContentReturnAuditDTO dto);

    /**
     * 我的发布内容
     */
    List<Content> myList(Long userId);
}
