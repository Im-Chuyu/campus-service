package com.twilight.campus.service.impl;

import com.twilight.campus.constant.ResultCodeConstant;
import com.twilight.campus.dto.FriendCategoryAssignDTO;
import com.twilight.campus.dto.FriendCategorySaveDTO;
import com.twilight.campus.exception.BusinessException;
import com.twilight.campus.mapper.FriendCategoryMapper;
import com.twilight.campus.mapper.UserFriendMapper;
import com.twilight.campus.mapper.UserMapper;
import com.twilight.campus.mapper.UserBlockMapper;
import com.twilight.campus.pojo.FriendCategory;
import com.twilight.campus.pojo.SysUser;
import com.twilight.campus.pojo.UserFriend;
import com.twilight.campus.service.FriendService;
import com.twilight.campus.utils.AuthUtil;
import com.twilight.campus.vo.FriendVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@Transactional
public class FriendServiceImpl implements FriendService {

    private static final int MAX_CATEGORY_COUNT = 20;
    private static final int MAX_FRIEND_COUNT = 500;
    private static final ConcurrentMap<Long, Object> CATEGORY_INIT_LOCKS = new ConcurrentHashMap<>();
    private static final String[] DEFAULT_CATEGORY_NAMES = {"我的好友", "家人", "朋友", "兄弟/姐妹", "老师", "陌生人"};

    @Autowired
    private UserFriendMapper userFriendMapper;

    @Autowired
    private FriendCategoryMapper friendCategoryMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserBlockMapper userBlockMapper;

    @Override
    public List<FriendVO> list() {
        SysUser currentUser = AuthUtil.getLoginUser();
        ensureDefaultCategories(currentUser.getId());
        return userFriendMapper.selectByUserId(currentUser.getId());
    }

    @Override
    public FriendVO search(String username) {
        SysUser currentUser = AuthUtil.getLoginUser();
        String keyword = username == null ? "" : username.trim();
        if (keyword.isEmpty()) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "请输入用户名");
        }

        SysUser user = userMapper.selectByUsername(keyword);
        if (user == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }
        if (user.getId().equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "不能添加自己为好友");
        }

        FriendVO vo = new FriendVO();
        vo.setFriendId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setSignature(user.getSignature());
        vo.setAvatar(user.getAvatar());
        vo.setProfileBackground(user.getProfileBackground());
        vo.setRoleId(user.getRoleId());
        vo.setPrivateChatEnabled(user.getPrivateChatEnabled());
        vo.setStatus(user.getStatus());
        vo.setIsFriend(userFriendMapper.countByUserAndFriend(currentUser.getId(), user.getId()) > 0 ? 1 : 0);
        return vo;
    }

    @Override
    public void add(Long friendId) {
        SysUser currentUser = AuthUtil.getLoginUser();
        if (friendId == null || friendId.equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "不能添加自己为好友");
        }

        SysUser friend = userMapper.selectById(friendId);
        if (friend == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "用户不存在");
        }
        if (friend.getStatus() == null || friend.getStatus() != 1) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "该用户账号不可用");
        }
        if (userBlockMapper.countByBlockerAndBlocked(friendId, currentUser.getId()) > 0) {
            throw new BusinessException(ResultCodeConstant.FORBIDDEN, "对方已屏蔽你，不能添加好友");
        }
        if (userFriendMapper.countByUserAndFriend(currentUser.getId(), friendId) > 0) {
            return;
        }
        if (userFriendMapper.countByUserId(currentUser.getId()) >= MAX_FRIEND_COUNT) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "好友数量最多500个");
        }

        FriendCategory defaultCategory = ensureDefaultCategories(currentUser.getId());
        UserFriend userFriend = new UserFriend();
        userFriend.setUserId(currentUser.getId());
        userFriend.setFriendId(friendId);
        userFriend.setCategoryId(defaultCategory.getId());
        userFriendMapper.insert(userFriend);
    }

    @Override
    public void delete(Long friendId) {
        SysUser currentUser = AuthUtil.getLoginUser();
        if (friendId == null || friendId.equals(currentUser.getId())) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "好友用户不正确");
        }
        userFriendMapper.deleteByUserAndFriend(currentUser.getId(), friendId);
    }

    @Override
    public int status(Long friendId) {
        SysUser currentUser = AuthUtil.getLoginUser();
        if (friendId == null || friendId.equals(currentUser.getId())) {
            return 0;
        }
        return userFriendMapper.countByUserAndFriend(currentUser.getId(), friendId) > 0 ? 1 : 0;
    }

    @Override
    public List<FriendCategory> categories() {
        SysUser currentUser = AuthUtil.getLoginUser();
        ensureDefaultCategories(currentUser.getId());
        return friendCategoryMapper.selectByUserId(currentUser.getId());
    }

    @Override
    public void saveCategory(FriendCategorySaveDTO dto) {
        SysUser currentUser = AuthUtil.getLoginUser();
        ensureDefaultCategories(currentUser.getId());
        String name = dto.getName().trim();
        if (friendCategoryMapper.countByUserIdAndName(currentUser.getId(), name, dto.getId()) > 0) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "分类名称已存在");
        }

        if (dto.getId() == null) {
            int count = friendCategoryMapper.countByUserId(currentUser.getId());
            if (count >= MAX_CATEGORY_COUNT) {
                throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "好友分类最多20个");
            }
            FriendCategory category = new FriendCategory();
            category.setUserId(currentUser.getId());
            category.setName(name);
            category.setSort(count + 1);
            friendCategoryMapper.insert(category);
            return;
        }

        FriendCategory oldCategory = friendCategoryMapper.selectByIdAndUserId(dto.getId(), currentUser.getId());
        if (oldCategory == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "分类不存在");
        }
        oldCategory.setName(name);
        friendCategoryMapper.update(oldCategory);
    }

    @Override
    public void deleteCategory(Long id) {
        SysUser currentUser = AuthUtil.getLoginUser();
        ensureDefaultCategories(currentUser.getId());
        FriendCategory category = friendCategoryMapper.selectByIdAndUserId(id, currentUser.getId());
        if (category == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "分类不存在");
        }

        List<FriendCategory> categories = friendCategoryMapper.selectByUserId(currentUser.getId());
        FriendCategory target = categories.stream()
                .filter(item -> !item.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (target == null) {
            throw new BusinessException(ResultCodeConstant.BAD_REQUEST, "至少保留一个好友分类");
        }
        userFriendMapper.moveCategoryFriends(currentUser.getId(), id, target.getId());
        friendCategoryMapper.deleteByIdAndUserId(id, currentUser.getId());
    }

    @Override
    public void assignCategory(FriendCategoryAssignDTO dto) {
        SysUser currentUser = AuthUtil.getLoginUser();
        ensureDefaultCategories(currentUser.getId());
        FriendCategory category = friendCategoryMapper.selectByIdAndUserId(dto.getCategoryId(), currentUser.getId());
        if (category == null) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "分类不存在");
        }
        if (userFriendMapper.countByUserAndFriend(currentUser.getId(), dto.getFriendId()) <= 0) {
            throw new BusinessException(ResultCodeConstant.NOT_FOUND, "好友不存在");
        }
        userFriendMapper.updateCategory(currentUser.getId(), dto.getFriendId(), dto.getCategoryId());
    }

    private FriendCategory ensureDefaultCategories(Long userId) {
        FriendCategory first = friendCategoryMapper.selectFirstByUserId(userId);
        if (first != null) {
            return first;
        }

        Object lock = CATEGORY_INIT_LOCKS.computeIfAbsent(userId, ignored -> new Object());
        synchronized (lock) {
            try {
                first = friendCategoryMapper.selectFirstByUserId(userId);
                if (first != null) {
                    return first;
                }

                for (int i = 0; i < DEFAULT_CATEGORY_NAMES.length; i++) {
                    FriendCategory category = new FriendCategory();
                    category.setUserId(userId);
                    category.setName(DEFAULT_CATEGORY_NAMES[i]);
                    category.setSort(i + 1);
                    friendCategoryMapper.insertIgnore(category);
                }
                return friendCategoryMapper.selectFirstByUserId(userId);
            } finally {
                CATEGORY_INIT_LOCKS.remove(userId, lock);
            }
        }
    }
}
