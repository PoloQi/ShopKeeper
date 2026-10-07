package org.example.shopkeeper_backend.service;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.BusinessException;
import org.example.shopkeeper_backend.dto.LoginDTO;
import org.example.shopkeeper_backend.entity.SysUser;
import org.example.shopkeeper_backend.interceptor.LoginInterceptor;
import org.example.shopkeeper_backend.mapper.SysUserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final SysUserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 登录校验，成功后用户信息（不含密码）写入 session
     */
    public SysUser login(LoginDTO dto, HttpSession session) {
        SysUser user = userMapper.selectByUsername(dto.getUsername());
        if (user == null || !passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getStatus() == 0) {
            throw new BusinessException("该账号已停用，请联系管理员");
        }
        user.setPassword(null);
        session.setAttribute(LoginInterceptor.SESSION_USER, user);
        return user;
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }
}
