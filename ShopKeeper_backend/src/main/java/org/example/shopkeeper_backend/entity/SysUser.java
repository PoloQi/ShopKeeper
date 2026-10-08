package org.example.shopkeeper_backend.entity;

import lombok.Data;

/**
 * 用户（系统操作人员）
 */
@Data
public class SysUser {

    /** 用户编号 */
    private Long userId;

    /** 登录用户名 */
    private String username;

    /** BCrypt 密码密文 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 性别 M 男 / F 女 */
    private String gender;

    /** 1 店长 / 0 店员 */
    private Integer role;

    /** 1 启用 / 0 停用 */
    private Integer status;
}
