package org.example.shopkeeper_backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.shopkeeper_backend.common.OwnerOnly;
import org.example.shopkeeper_backend.entity.SysUser;
import org.example.shopkeeper_backend.mapper.SysUserMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 店长权限拦截器：@OwnerOnly 标注的类/方法要求库中用户 role=1 且启用，否则 403。
 * role/status 每次回查数据库，保证降权、停用、删除在当次请求即生效。
 */
@Component
public class OwnerInterceptor implements HandlerInterceptor {

    private final SysUserMapper sysUserMapper;

    public OwnerInterceptor(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        OwnerOnly ownerOnly = handlerMethod.getMethodAnnotation(OwnerOnly.class);
        if (ownerOnly == null) {
            ownerOnly = handlerMethod.getBeanType().getAnnotation(OwnerOnly.class);
        }
        if (ownerOnly == null) {
            return true;
        }
        HttpSession session = request.getSession(false);
        SysUser sessionUser = session != null
                ? (SysUser) session.getAttribute(LoginInterceptor.SESSION_USER)
                : null;
        if (sessionUser == null) {
            return writeForbidden(response);
        }
        // 回查库：降权、停用、删除当次请求即生效，不使用会话中的旧角色
        SysUser current = sysUserMapper.selectById(sessionUser.getUserId());
        if (current != null
                && Integer.valueOf(1).equals(current.getRole())
                && Integer.valueOf(1).equals(current.getStatus())) {
            return true;
        }
        return writeForbidden(response);
    }

    private boolean writeForbidden(HttpServletResponse response) throws java.io.IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"需要店长权限\"}");
        return false;
    }
}
