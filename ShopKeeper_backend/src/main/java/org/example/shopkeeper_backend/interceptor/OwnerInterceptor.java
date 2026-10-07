package org.example.shopkeeper_backend.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.shopkeeper_backend.common.OwnerOnly;
import org.example.shopkeeper_backend.entity.SysUser;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 店长权限拦截器：@OwnerOnly 标注的类/方法要求 session 用户 role=1，否则 403
 */
@Component
public class OwnerInterceptor implements HandlerInterceptor {

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
        SysUser user = session != null ? (SysUser) session.getAttribute(LoginInterceptor.SESSION_USER) : null;
        if (user != null && Integer.valueOf(1).equals(user.getRole())) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"需要店长权限\"}");
        return false;
    }
}
