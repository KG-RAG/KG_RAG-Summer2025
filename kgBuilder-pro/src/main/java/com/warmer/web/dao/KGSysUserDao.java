package com.warmer.web.dao;

import com.warmer.web.entity.KGSysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface KGSysUserDao {
    KGSysUser selectByUserNameAndPasswordAndIsTeacher(@Param("userName") String userName, @Param("password") String password, @Param("isTeacher") Integer isTeacher);
    KGSysUser selectByUserName(@Param("userName") String userName);
    void insertUser(KGSysUser user);
}
