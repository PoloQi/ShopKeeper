package org.example.shopkeeper_backend.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.shopkeeper_backend.entity.SysUser;

import java.util.List;

public interface SysUserMapper {

    /** 登录：按用户名查用户（含密码密文） */
    SysUser selectByUsername(@Param("username") String username);

    long count(@Param("username") String username,
               @Param("realName") String realName,
               @Param("status") Integer status);

    List<SysUser> selectPage(@Param("username") String username,
                             @Param("realName") String realName,
                             @Param("status") Integer status,
                             @Param("offset") int offset,
                             @Param("size") int size);

    SysUser selectById(@Param("userId") Long userId);

    int insert(SysUser user);

    int update(SysUser user);

    int deleteById(@Param("userId") Long userId);
}
