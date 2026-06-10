package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.ActivitySubCategorySaveDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.ActivitySubCategoryMapper;
import com.twilight.campus.pojo.ActivitySubCategory;
import com.twilight.campus.service.ActivitySubCategoryService;
import com.twilight.campus.utils.AuthUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivitySubCategoryServiceImpl implements ActivitySubCategoryService {

    @Autowired
    private ActivitySubCategoryMapper activitySubCategoryMapper;

    @Override
    public List<ActivitySubCategory> list(Boolean includeDisabled) {
        if (Boolean.TRUE.equals(includeDisabled)) {
            AuthUtil.checkAdmin();
            return activitySubCategoryMapper.selectAll();
        }
        return activitySubCategoryMapper.selectEnabled();
    }

    @Override
    public ActivitySubCategory getById(Long id) {
        ActivitySubCategory category = activitySubCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "活动通知分类不存在");
        }
        return category;
    }

    @Override
    public void add(ActivitySubCategorySaveDTO dto) {
        AuthUtil.checkAdmin();
        validateName(dto.getName());
        ActivitySubCategory exist = activitySubCategoryMapper.selectByName(dto.getName().trim());
        if (exist != null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "活动通知分类名称已存在");
        }

        ActivitySubCategory category = new ActivitySubCategory();
        BeanUtils.copyProperties(dto, category);
        category.setName(dto.getName().trim());
        fillDefault(category);
        activitySubCategoryMapper.insert(category);
    }

    @Override
    public void update(ActivitySubCategorySaveDTO dto) {
        AuthUtil.checkAdmin();
        if (dto.getId() == null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "活动通知分类ID不能为空");
        }
        validateName(dto.getName());

        ActivitySubCategory oldCategory = activitySubCategoryMapper.selectById(dto.getId());
        if (oldCategory == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "活动通知分类不存在");
        }

        ActivitySubCategory sameNameCategory = activitySubCategoryMapper.selectByName(dto.getName().trim());
        if (sameNameCategory != null && !sameNameCategory.getId().equals(dto.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "活动通知分类名称已存在");
        }

        ActivitySubCategory category = new ActivitySubCategory();
        BeanUtils.copyProperties(dto, category);
        category.setName(dto.getName().trim());
        fillDefault(category);
        activitySubCategoryMapper.update(category);
    }

    @Override
    public void deleteById(Long id) {
        AuthUtil.checkAdmin();
        ActivitySubCategory category = activitySubCategoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "活动通知分类不存在");
        }

        int contentCount = activitySubCategoryMapper.countContentById(id);
        if (contentCount > 0) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "该活动通知分类下已有文章，不能删除。可以先停用该分类。");
        }
        activitySubCategoryMapper.deleteById(id);
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "活动通知分类名称不能为空");
        }
        if (name.trim().length() > 50) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "活动通知分类名称不能超过50个字符");
        }
    }

    private void fillDefault(ActivitySubCategory category) {
        if (category.getSort() == null) {
            category.setSort(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }
    }
}
