package com.twilight.campus.service;

import com.twilight.campus.dto.ActivitySubCategorySaveDTO;
import com.twilight.campus.pojo.ActivitySubCategory;

import java.util.List;

public interface ActivitySubCategoryService {

    List<ActivitySubCategory> list(Boolean includeDisabled);

    ActivitySubCategory getById(Long id);

    void add(ActivitySubCategorySaveDTO dto);

    void update(ActivitySubCategorySaveDTO dto);

    void deleteById(Long id);
}
