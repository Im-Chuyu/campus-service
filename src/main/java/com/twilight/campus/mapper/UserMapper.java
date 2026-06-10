package com.twilight.campus.mapper;

import com.twilight.campus.pojo.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    SysUser selectByUsername(String username);

    SysUser selectByPhone(String phone);

    SysUser selectByEmail(String email);

    SysUser selectById(Long id);

    int insert(SysUser user);

    int updatePassword(SysUser user);

    int updateUser(SysUser user);

    List<SysUser> selectList(@Param("keyword") String keyword,
                             @Param("status") Integer status,
                             @Param("roleId") Long roleId);

    Long countList(@Param("keyword") String keyword,
                   @Param("status") Integer status,
                   @Param("roleId") Long roleId);

    List<SysUser> selectPage(@Param("keyword") String keyword,
                             @Param("status") Integer status,
                             @Param("roleId") Long roleId,
                             @Param("offset") Integer offset,
                             @Param("pageSize") Integer pageSize);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    int deleteById(Long id);
}
