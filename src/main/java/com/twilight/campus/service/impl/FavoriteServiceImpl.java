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
import com.twilight.campus.vo.FavoriteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 收藏业务实现类
 */
@Service
@Transactional
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ContentMapper contentMapper;

    @Override
    public void add(Long contentId) {
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        Content content = contentMapper.selectById(contentId);
        if (content == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "内容不存在");
        }

        Favorite exist = favoriteMapper.selectByUserIdAndContentId(currentUser.getId(), contentId);
        if (exist != null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "你已经收藏过了");
        }

        Favorite favorite = new Favorite();
        favorite.setUserId(currentUser.getId());
        favorite.setContentId(contentId);
        favoriteMapper.insert(favorite);

        contentMapper.increaseFavoriteCount(contentId);
    }

    @Override
    public void delete(Long contentId) {
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }

        Favorite exist = favoriteMapper.selectByUserIdAndContentId(currentUser.getId(), contentId);
        if (exist == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "你还没有收藏这个内容");
        }

        favoriteMapper.deleteByUserIdAndContentId(currentUser.getId(), contentId);
        contentMapper.decreaseFavoriteCount(contentId);
    }

    @Override
    public List<FavoriteVO> myList() {
        SysUser currentUser = UserContext.getUser();
        if (currentUser == null) {
            throw new BusinessException(ResultCodeConstant.UNAUTHORIZED, "未登录");
        }
        return favoriteMapper.selectByUserId(currentUser.getId());
    }
}
