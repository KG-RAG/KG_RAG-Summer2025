package com.warmer.web.entity;

import lombok.Data;

import java.util.Date;
@Data
public class KGSysUser {
    private Integer id;
    private String userName;
    private String password;
    // 0:学生 1:老师
    private Integer isTeacher;
    private Date createTime;
    private Date updateTime;
}
