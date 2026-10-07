package org.example.shopkeeper_backend.interceptor;

import org.example.shopkeeper_backend.common.OwnerOnly;
import org.example.shopkeeper_backend.entity.SysUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.method.HandlerMethod;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    private OwnerInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new OwnerInterceptor();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    private HandlerMethod handler(Object bean, String method) throws NoSuchMethodException {
        return new HandlerMethod(bean, bean.getClass().getMethod(method));
    }

    private void loginWithRole(Integer role) {
        SysUser user = new SysUser();
        user.setRole(role);
        request.getSession().setAttribute(LoginInterceptor.SESSION_USER, user);
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
        loginWithRole(1);
        PlainApi bean = new PlainApi();
        assertTrue(interceptor.preHandle(request, response, handler(bean, "methodLevelProtected")));
    }

    @Test
    void clerkBlockedMethodLevel() throws Exception {
        loginWithRole(0);
        PlainApi bean = new PlainApi();
        assertFalse(interceptor.preHandle(request, response, handler(bean, "methodLevelProtected")));
        assertEquals(403, response.getStatus());
    }

    @Test
    void clerkBlockedClassLevel() throws Exception {
        loginWithRole(0);
        ProtectedApi bean = new ProtectedApi();
        assertFalse(interceptor.preHandle(request, response, handler(bean, "classLevelProtected")));
        assertEquals(403, response.getStatus());
    }

    @Test
    void ownerPassesClassLevel() throws Exception {
        loginWithRole(1);
        ProtectedApi bean = new ProtectedApi();
        assertTrue(interceptor.preHandle(request, response, handler(bean, "classLevelProtected")));
    }

    @Test
    void noSessionBlocked() throws Exception {
        PlainApi bean = new PlainApi();
        assertFalse(interceptor.preHandle(request, response, handler(bean, "methodLevelProtected")));
        assertEquals(403, response.getStatus());
    }
}
