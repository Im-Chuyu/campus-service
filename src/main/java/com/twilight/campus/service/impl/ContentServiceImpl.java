package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.ContentAddDTO;
import com.twilight.campus.dto.ContentQueryDTO;
import com.twilight.campus.dto.ContentUpdateDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.CategoryMapper;
import com.twilight.campus.mapper.ContentMapper;
import com.twilight.campus.pojo.Category;
import com.twilight.campus.pojo.Content;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.ContentService;
import com.twilight.campus.utils.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 内容业务实现类
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public List<Content> list(ContentQueryDTO query) {
        return contentMapper.selectList(query);
    }

    @Override
    public Content getById(Long id) {
        Content content = contentMapper.selectById(id);
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }
        return content;
    }

    @Override
    public void add(ContentAddDTO dto) {
        // 1. 检查分类是否存在
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "分类不存在");
        }

        // 2. 获取当前登录用户
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        // 3. 组装内容对象
        Content content = new Content();
        BeanUtils.copyProperties(dto, content);

        content.setUserId(currentUser.getId());

        // 新内容默认待审核
        content.setStatus(0);
        content.setAuditReason(null);

        // 统计字段默认值
        content.setIsTop(0);
        content.setViewCount(0);
        content.setLikeCount(0);
        content.setFavoriteCount(0);
        content.setCommentCount(0);

        contentMapper.insert(content);
    }

    @Override
    public void update(ContentUpdateDTO dto) {
        // 1. 查内容是否存在
        Content oldContent = contentMapper.selectById(dto.getId());
        if (oldContent == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }

        // 2. 检查分类是否存在
        Category category = categoryMapper.selectById(dto.getCategoryId());
        if (category == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "分类不存在");
        }

        // 3. 检查是否是当前用户自己的内容
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        if (!oldContent.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只能修改自己的内容");
        }

        // 4. 组装更新对象
        Content content = new Content();
        BeanUtils.copyProperties(dto, content);

        contentMapper.update(content);
    }

    @Override
    public void deleteById(Long id) {
        // 1. 查内容是否存在
        Content oldContent = contentMapper.selectById(id);
        if (oldContent == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }

        // 2. 检查是否是当前用户自己的内容
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        if (!oldContent.getUserId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "只能删除自己的内容");
        }

        contentMapper.deleteById(id);
    }

    @Override
    public List<Content> myList(Long userId) {
        return contentMapper.selectByUserId(userId);
    }
}
