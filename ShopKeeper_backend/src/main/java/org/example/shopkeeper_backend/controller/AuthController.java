package org.example.shopkeeper_backend.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.Result;
import org.example.shopkeeper_backend.dto.LoginDTO;
import org.example.shopkeeper_backend.entity.SysUser;
import org.example.shopkeeper_backend.interceptor.LoginInterceptor;
import org.example.shopkeeper_backend.service.AuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /** 登录 */
    @PostMapping("/login")
    public Result<SysUser> login(@Valid @RequestBody LoginDTO dto, HttpSession session) {
        return Result.ok(authService.login(dto, session));
    }

    /** 登出 */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        authService.logout(session);
        return Result.ok();
    }

    /** 当前登录用户信息 */
    @GetMapping("/info")
    public Result<SysUser> info(HttpSession session) {
        return Result.ok((SysUser) session.getAttribute(LoginInterceptor.SESSION_USER));
    }
}
