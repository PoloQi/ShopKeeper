package org.example.shopkeeper_backend.interceptor;

import org.example.shopkeeper_backend.common.OwnerOnly;
import org.example.shopkeeper_backend.entity.SysUser;
import org.example.shopkeeper_backend.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OwnerInterceptorTest {

    @OwnerOnly
    static class ProtectedApi {
        public void classLevelProtected() {
        }
    }

    static class PlainApi {
        @OwnerOnly
        public void methodLevelProtected() {
        }

        public void open() {
        }
    }

    private SysUserMapper sysUserMapper;
    private OwnerInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        sysUserMapper = mock(SysUserMapper.class);
        interceptor = new OwnerInterceptor(sysUserMapper);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    private HandlerMethod handler(Object bean, String method) throws NoSuchMethodException {
        return new HandlerMethod(bean, bean.getClass().getMethod(method));
    }

    /** 模拟已登录会话：携带 userId 与登录时的旧 role（当前角色以数据库为准） */
    private void login(Long userId, Integer sessionRole) {
        SysUser sessionUser = new SysUser();
        sessionUser.setUserId(userId);
        sessionUser.setRole(sessionRole);
        request.getSession().setAttribute(LoginInterceptor.SESSION_USER, sessionUser);
    }

    /** 模拟数据库中该用户当前的角色与状态 */
    private void dbUser(Long userId, Integer role, Integer status) {
        SysUser dbUser = new SysUser();
        dbUser.setUserId(userId);
        dbUser.setRole(role);
        dbUser.setStatus(status);
        when(sysUserMapper.selectById(userId)).thenReturn(dbUser);
    }

    @Test
    void noAnnotationAlwaysPasses() throws Exception {
        PlainApi bean = new PlainApi();
        assertTrue(interceptor.preHandle(request, response, handler(bean, "open")));
    }

    @Test
    void nonHandlerMethodPasses() throws Exception {
        // 静态资源等非 HandlerMethod 直接放行
        assertTrue(interceptor.preHandle(request, response, new Object()));
    }

    @Test
    void ownerPassesMethodLevel() throws Exception {
        login(1L, 1);
        dbUser(1L, 1, 1);
        PlainApi bean = new PlainApi();
        assertTrue(interceptor.preHandle(request, response, handler(bean, "methodLevelProtected")));
    }

    @Test
    void clerkBlockedMethodLevel() throws Exception {
        login(2L, 0);
        dbUser(2L, 0, 1);
        PlainApi bean = new PlainApi();
        assertFalse(interceptor.preHandle(request, response, handler(bean, "methodLevelProtected")));
        assertEquals(403, response.getStatus());
    }

    @Test
    void clerkBlockedClassLevel() throws Exception {
        login(2L, 0);
        dbUser(2L, 0, 1);
        ProtectedApi bean = new ProtectedApi();
        assertFalse(interceptor.preHandle(request, response, handler(bean, "classLevelProtected")));
        assertEquals(403, response.getStatus());
    }

    @Test
    void ownerPassesClassLevel() throws Exception {
        login(1L, 1);
        dbUser(1L, 1, 1);
        ProtectedApi bean = new ProtectedApi();
        assertTrue(interceptor.preHandle(request, response, handler(bean, "classLevelProtected")));
    }

    @Test
    void noSessionBlocked() throws Exception {
        PlainApi bean = new PlainApi();
        assertFalse(interceptor.preHandle(request, response, handler(bean, "methodLevelProtected")));
        assertEquals(403, response.getStatus());
    }

    @Test
    void ownerDemotedInDbIsBlocked() throws Exception {
        // 会话是旧的店长会话，但库里已被降为店员
        login(1L, 1);
        dbUser(1L, 0, 1);
        ProtectedApi bean = new ProtectedApi();
        assertFalse(interceptor.preHandle(request, response, handler(bean, "classLevelProtected")));
        assertEquals(403, response.getStatus());
    }

    @Test
    void ownerDisabledInDbIsBlocked() throws Exception {
        // 店长账号被停用
        login(1L, 1);
        dbUser(1L, 1, 0);
        ProtectedApi bean = new ProtectedApi();
        assertFalse(interceptor.preHandle(request, response, handler(bean, "classLevelProtected")));
        assertEquals(403, response.getStatus());
    }

    @Test
    void userDeletedFromDbIsBlocked() throws Exception {
        // 用户已从数据库删除，旧会话仍在
        login(1L, 1);
        when(sysUserMapper.selectById(1L)).thenReturn(null);
        ProtectedApi bean = new ProtectedApi();
        assertFalse(interceptor.preHandle(request, response, handler(bean, "classLevelProtected")));
        assertEquals(403, response.getStatus());
    }
}
