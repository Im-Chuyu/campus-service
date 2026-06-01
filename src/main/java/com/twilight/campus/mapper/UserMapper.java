package com.twilight.campus.mapper;

import com.twilight.campus.pojo.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    SysUser selectByUsername(String username);

    SysUser selectById(Long id);

    int insert(SysUser user);

    int updatePassword(SysUser user);

    int updateUser(SysUser user);
}
