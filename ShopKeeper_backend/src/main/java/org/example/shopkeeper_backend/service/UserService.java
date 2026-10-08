package org.example.shopkeeper_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.BusinessException;
import org.example.shopkeeper_backend.common.PageResult;
import org.example.shopkeeper_backend.entity.SysUser;
import org.example.shopkeeper_backend.mapper.SysUserMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final SysUserMapper userMapper;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public PageResult<SysUser> page(String username, String realName, Integer role, Integer status,
                                    int page, int size) {
        long total = userMapper.count(username, realName, role, status);
        List<SysUser> records = total == 0
                ? List.of()
                : userMapper.selectPage(username, realName, role, status, (page - 1) * size, size);
        return new PageResult<>(total, records);
    }

    /** 新增：用户名唯一，密码 BCrypt 加密后存储 */
    public void add(SysUser user) {
        checkUsernameUnique(user.getUsername(), null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
    }

    /** 修改：密码留空表示不修改 */
    public void update(SysUser user) {
        checkUsernameUnique(user.getUsername(), user.getUserId());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        userMapper.update(user);
    }

    /** 删除：禁止删除当前登录账号 */
    public void delete(Long userId, Long loginUserId) {
        if (userId.equals(loginUserId)) {
            throw new BusinessException("不能删除当前登录账号");
        }
        userMapper.deleteById(userId);
    }

    private void checkUsernameUnique(String username, Long userId) {
        SysUser exist = userMapper.selectByUsername(username);
        if (exist != null && !exist.getUserId().equals(userId)) {
            throw new BusinessException("用户名已存在");
        }
    }
}
