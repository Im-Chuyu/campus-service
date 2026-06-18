package com.twilight.campus.mapper;

import com.twilight.campus.pojo.UserFriend;
import com.twilight.campus.vo.FriendVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserFriendMapper {
    int insert(UserFriend friend);

    int deleteByUserAndFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);

    int countByUserAndFriend(@Param("userId") Long userId, @Param("friendId") Long friendId);

    int countByUserId(Long userId);

    List<FriendVO> selectByUserId(Long userId);

    int updateCategory(@Param("userId") Long userId, @Param("friendId") Long friendId, @Param("categoryId") Long categoryId);

    int moveCategoryFriends(@Param("userId") Long userId, @Param("oldCategoryId") Long oldCategoryId, @Param("newCategoryId") Long newCategoryId);
}
