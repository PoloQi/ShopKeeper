package org.example.shopkeeper_backend.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.OwnerOnly;
import org.example.shopkeeper_backend.common.PageResult;
import org.example.shopkeeper_backend.common.Result;
import org.example.shopkeeper_backend.entity.SysUser;
import org.example.shopkeeper_backend.interceptor.LoginInterceptor;
import org.example.shopkeeper_backend.service.UserService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@OwnerOnly
public class UserController {

    private final UserService userService;

    /** 分页 + 多条件组合查询（列表不返回密码） */
    @GetMapping
    public Result<PageResult<SysUser>> page(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer role,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userService.page(username, realName, role, status, page, size));
    }

    @PostMapping
    public Result<Void> add(@RequestBody SysUser user) {
        userService.add(user);
        return Result.ok();
    }

    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        userService.update(user);
        return Result.ok();
    }

    @DeleteMapping("/{userId}")
    public Result<Void> delete(@PathVariable Long userId, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute(LoginInterceptor.SESSION_USER);
        userService.delete(userId, loginUser.getUserId());
        return Result.ok();
    }
}
