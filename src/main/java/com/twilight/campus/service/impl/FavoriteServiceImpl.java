package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.ContentMapper;
import com.twilight.campus.mapper.FavoriteMapper;
import com.twilight.campus.pojo.Content;
import com.twilight.campus.pojo.Favorite;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.service.FavoriteService;
import com.twilight.campus.utils.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 收藏业务实现类
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ContentMapper contentMapper;

    @Override
    public void add(Long contentId) {
        // 1. 判断登录
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        // 2. 判断内容是否存在
        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }

        // 3. 是否已经收藏过
        Favorite exist = favoriteMapper.selectByUserIdAndContentId(currentUser.getId(), contentId);
        if (exist != null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "你已经收藏过了");
        }

        // 4. 新增收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(currentUser.getId());
        favorite.setContentId(contentId);
        favoriteMapper.insert(favorite);

        // 5. 收藏数 +1
        content.setFavoriteCount(content.getFavoriteCount() == null ? 1 : content.getFavoriteCount() + 1);
        contentMapper.update(content);
    }

    @Override
    public void delete(Long contentId) {
        // 1. 判断登录
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        // 2. 判断是否存在收藏记录
        Favorite exist = favoriteMapper.selectByUserIdAndContentId(currentUser.getId(), contentId);
        if (exist == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "你还没有收藏这个内容");
        }

        // 3. 删除收藏
        favoriteMapper.deleteByUserIdAndContentId(currentUser.getId(), contentId);

        // 4. 收藏数 -1
        Content content = contentMapper.selectById(contentId);
        if (content != null && content.getFavoriteCount() != null && content.getFavoriteCount() > 0) {
            content.setFavoriteCount(content.getFavoriteCount() - 1);
            contentMapper.update(content);
        }
    }

    @Override
    public List<Favorite> myList() {
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }
        return favoriteMapper.selectByUserId(currentUser.getId());
    }
}
