package com.twilight.campus.mapper;

import com.twilight.campus.pojo.ActivitySubCategory;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ActivitySubCategoryMapper {

    List<ActivitySubCategory> selectAll();

    List<ActivitySubCategory> selectEnabled();

    ActivitySubCategory selectById(Long id);

    ActivitySubCategory selectByName(String name);

    int countContentById(Long id);

    int insert(ActivitySubCategory activitySubCategory);

    int update(ActivitySubCategory activitySubCategory);

    int deleteById(Long id);
}
