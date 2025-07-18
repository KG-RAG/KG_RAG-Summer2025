package com.warmer.web.controller;

import com.warmer.web.dao.KGSysUserDao;
import com.warmer.web.entity.KGSysUser;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

//@RestController
//public class LoginController {
//
//    private final KGSysUserDao userMapper;
//
//    public LoginController(KGSysUserDao userMapper) {
//        this.userMapper = userMapper;
//    }
//
//    @PostMapping("/api/login")
//    public Map<String, Object> login(@RequestBody KGSysUser user) {
//        Map<String, Object> result = new HashMap<>();
//        KGSysUser dbUser = userMapper.selectByUserNameAndPasswordAndIsTeacher(
//                user.getUserName(), user.getPassword(), user.getIsTeacher());
//        System.out.println(dbUser);
//        if (dbUser != null) {
//            result.put("success", true);
//            result.put("message", "登录成功");
//        } else {
//            result.put("success", false);
//            result.put("message", "用户名、密码或角色错误");
//        }
//        return result;
//    }
//}
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoginController {

    private final KGSysUserDao userMapper;

    public LoginController(KGSysUserDao userMapper) {
        this.userMapper = userMapper;
    }

    @PostMapping("/api/login")
    public Map<String, Object> login(@RequestBody KGSysUser user) {
        Map<String, Object> result = new HashMap<>();
        KGSysUser dbUser = userMapper.selectByUserNameAndPasswordAndIsTeacher(
                user.getUserName(), user.getPassword(), user.getIsTeacher());
        System.out.println(dbUser);
        if (dbUser != null) {
            result.put("success", true);
            result.put("message", "登录成功");
        } else {
            result.put("success", false);
            result.put("message", "用户名、密码或角色错误");
        }
        return result;
    }

    @PostMapping("/api/register")
    public Map<String, Object> register(@RequestBody KGSysUser user) {
        Map<String, Object> result = new HashMap<>();
        // 检查用户名是否已存在
        KGSysUser existingUser = userMapper.selectByUserName(user.getUserName());
        if (existingUser != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
        } else {
            // 插入新用户
            userMapper.insertUser(user);
            result.put("success", true);
            result.put("message", "注册成功");
        }
        return result;
    }
}
