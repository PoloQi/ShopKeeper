# 电话校验、审核详情页与两级权限 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为店管家系统补齐客户/供应商电话格式校验、把单据审核改为进详情页查看后审核、并落地「店长 / 店员」两级权限。

**Architecture:** 后端在 common 包加 `PhoneValidator` 与 `@OwnerOnly` 注解，用一个 `OwnerInterceptor` 按方法/类注解拦截，返回 403；`sys_user` 表加 `role` 字段。前端编辑页改造为 edit/audit/view 三模式复用同一组件，菜单、路由守卫和列表按钮按 `user.role` 呈现。

**Tech Stack:** Spring Boot 3 / MyBatis / JDK 17 / JUnit 5（spring-test 的 MockHttpServletRequest，不启动 Spring 容器）；React 18 / Ant Design 5 / React Router 6；MySQL 8（库名 jxc_db）。

**Spec:** docs/superpowers/specs/2026-10-07-phone-audit-role-design.md（计划与 spec 配套，执行时两份都读）

## Global Constraints

- 后端基础包名 `org.example.shopkeeper_backend`，代码放在 `ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/`，测试放 `ShopKeeper_backend/src/test/java/org/example/shopkeeper_backend/`。
- Mapper XML 在 `ShopKeeper_backend/src/main/resources/mapper/`。
- 角色：店长 `role=1`、店员 `role=0`；界面文案统一用「店长 / 店员」，不出现「超级管理员」字样。
- 电话正则：`^(1[3-9]\d{9}|0\d{2,3}-?\d{7,8})$`；非必填，填写才校验。
- 前端源码在 `ShopKeeper_frontend/src/`，界面语言中文，遵循现有文件风格（函数组件 + antd 按需具名导入）。
- SQL 脚本变更只写文件并提示用户，由用户本人在 MySQL 执行；执行者不直连数据库。
- 只做本计划列出的改动，不重构无关代码；提交信息用中文 conventional commits（feat/fix/docs）。
- 后端单测命令统一加 `-Dtest=<类名>` 限定，避免触发需要 MySQL 的 `ShopKeeperBackendApplicationTests`。

## Review Focus

1. **类级 @OwnerOnly 必须覆盖全部 HTTP 方法**：店员对 `/api/users` 的 GET 之外，POST 新增、PUT 修改、DELETE 也必须 403，而不是只有 GET 被拦。→ Task 5 的 curl 矩阵覆盖 POST/PUT/DELETE。
2. **首尾带空格的电话**：`" 13800138000 "` 应 strip 后通过，而不是误判非法。→ Task 1 的 PhoneValidatorTest 有此用例。
3. **已审核单据进入审核页**：店长从列表对已审核单据只能「查看」；若手工访问 `/purchase/audit/<已审核单号>`，页面应只读展示且无「审核通过」按钮（audit 按钮只在 audit 模式出现，但页面照常加载不崩溃）；重复调审核接口后端返回业务错误「该单据已审核」。→ Task 5 curl 重复审核 + Task 6 手工核对。
4. **店员绕过前端直接输 URL**：手输 `/user` 被 RequireOwner 重定向到 `/stock`；手输 `/purchase/audit/<单号>` 能看只读详情但没有「审核通过」按钮。→ Task 6、Task 7 手工步骤。
5. **认证接口不受权限拦截器影响**：加了 OwnerInterceptor 后 `/api/auth/login`、`/api/auth/info`、`/api/auth/logout` 行为不变（无 @OwnerOnly 即放行）。→ Task 5 curl 登录与 info。

---

## File Structure

**后端新建：**
- `common/PhoneValidator.java` —— 电话格式静态校验工具
- `common/OwnerOnly.java` —— 店长权限注解（类/方法）
- `interceptor/OwnerInterceptor.java` —— 读注解、查 session、返回 403

**后端修改：**
- `entity/SysUser.java` —— 加 role
- `mapper/SysUserMapper.java`、`resources/mapper/SysUserMapper.xml` —— role 列与 role 查询条件
- `service/CustomerService.java`、`service/SupplierService.java` —— 保存前校验电话
- `service/UserService.java` —— page 加 role 参数
- `config/WebConfig.java` —— 注册 OwnerInterceptor
- `controller/UserController.java`（类级）、`PurchaseController.java`/`SaleController.java`（audit + delete）、`CustomerController.java`/`SupplierController.java`/`ProductController.java`（delete）—— 加 @OwnerOnly

**后端测试新建：**
- `common/PhoneValidatorTest.java`、`interceptor/OwnerInterceptorTest.java`

**前端新建：**
- `src/utils/validators.js` —— phonePattern

**前端修改：**
- `src/App.jsx` —— 4 条 audit/view 路由 + RequireOwner
- `src/pages/PurchaseEdit.jsx`、`src/pages/SaleEdit.jsx` —— 三模式
- `src/pages/Purchase.jsx`、`src/pages/Sale.jsx` —— 审核跳转、已审核查看
- `src/pages/Customer.jsx`、`Supplier.jsx`、`Product.jsx`、`Purchase.jsx`、`Sale.jsx` —— 删除/审核按钮按角色灰化
- `src/layouts/MainLayout.jsx` —— 菜单过滤、角色显示
- `src/pages/User.jsx` —— 角色列、角色表单项、角色查询

**数据与文档：**
- 修改 `sql/init.sql`、`sql/test-data.sql`；新建 `sql/v2_add_role.sql`
- 修改 `docs/01-ER模型设计.md`、`docs/02-数据库设计.md`、`docs/03-ER图Mermaid代码.md`

---

### Task 1: 后端电话校验（PhoneValidator + 两个 Service）

**Files:**
- Create: `ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/common/PhoneValidator.java`
- Modify: `ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/service/CustomerService.java:36-46`
- Modify: `ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/service/SupplierService.java:36-46`
- Test: `ShopKeeper_backend/src/test/java/org/example/shopkeeper_backend/common/PhoneValidatorTest.java`

**Interfaces:**
- Consumes: `common/BusinessException`（已有构造器 `BusinessException(String message)`）
- Produces: `PhoneValidator.check(String phone)` —— null/空白放行；非法抛 BusinessException；无返回值。Task 2 的前端校验不依赖本类。

- [ ] **Step 1: 写失败测试**

创建 `PhoneValidatorTest.java`：

```java
package org.example.shopkeeper_backend.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneValidatorTest {

    @Test
    void blankPhonePasses() {
        assertDoesNotThrow(() -> PhoneValidator.check(null));
        assertDoesNotThrow(() -> PhoneValidator.check(""));
        assertDoesNotThrow(() -> PhoneValidator.check("   "));
    }

    @Test
    void validMobilePasses() {
        assertDoesNotThrow(() -> PhoneValidator.check("13800138000"));
    }

    @Test
    void validLandlinePasses() {
        assertDoesNotThrow(() -> PhoneValidator.check("020-12345678"));
        assertDoesNotThrow(() -> PhoneValidator.check("02012345678"));
        assertDoesNotThrow(() -> PhoneValidator.check("07551234567"));
    }

    @Test
    void surroundingSpacesAreStripped() {
        assertDoesNotThrow(() -> PhoneValidator.check("  13800138000 "));
    }

    @Test
    void invalidPhoneRejected() {
        assertThrows(BusinessException.class, () -> PhoneValidator.check("123456"));
        assertThrows(BusinessException.class, () -> PhoneValidator.check("12345678901"));
        assertThrows(BusinessException.class, () -> PhoneValidator.check("1380013800"));
        assertThrows(BusinessException.class, () -> PhoneValidator.check("abc"));
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run（在 `ShopKeeper_backend/` 下）：`mvn -Dtest=PhoneValidatorTest test`
Expected: 编译失败 / FAIL，原因「找不到符号 PhoneValidator」。

- [ ] **Step 3: 实现 PhoneValidator**

创建 `PhoneValidator.java`：

```java
package org.example.shopkeeper_backend.common;

import java.util.regex.Pattern;

/**
 * 联系电话校验：11位手机号 或 带区号固话；null/空白视为不填，放行
 */
public final class PhoneValidator {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})$");

    private PhoneValidator() {
    }

    public static void check(String phone) {
        if (phone == null || phone.isBlank()) {
            return;
        }
        // strip() 容忍录入时首尾误带的空格
        if (!PHONE_PATTERN.matcher(phone.strip()).matches()) {
            throw new BusinessException("联系电话格式不正确，请输入11位手机号或带区号的固话");
        }
    }
}
```

- [ ] **Step 4: 测试通过后接入两个 Service**

运行 `mvn -Dtest=PhoneValidatorTest test`，Expected: 5 个测试全 PASS。

然后改 `CustomerService.java`：`add` 方法首行（生成编号之前）加校验：

```java
    /** 新增：自动生成 KH+4位流水 编号，返回编号 */
    public String add(Customer customer) {
        PhoneValidator.check(customer.getPhone());
        String maxId = customerMapper.selectMaxId();
```

`update` 方法改为：

```java
    public void update(Customer customer) {
        PhoneValidator.check(customer.getPhone());
        customerMapper.update(customer);
    }
```

并在文件 import 区加（与现有 import 风格一致，放在 `lombok` import 之前的同组位置）：

```java
import org.example.shopkeeper_backend.common.PhoneValidator;
```

对 `SupplierService.java` 做对称修改：`add`、`update` 首行调用 `PhoneValidator.check(supplier.getPhone());`，加同一条 import。

- [ ] **Step 5: 编译并提交**

Run：`mvn -q compile`
Expected: BUILD SUCCESS。

```bash
git add ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/common/PhoneValidator.java \
        ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/service/CustomerService.java \
        ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/service/SupplierService.java \
        ShopKeeper_backend/src/test/java/org/example/shopkeeper_backend/common/PhoneValidatorTest.java
git commit -m "feat: 客户/供应商电话前后格式校验（后端）"
```

---

### Task 2: 前端电话校验（客户 + 供应商弹窗）

**Files:**
- Create: `ShopKeeper_frontend/src/utils/validators.js`
- Modify: `ShopKeeper_frontend/src/pages/Customer.jsx:18` 及电话 Form.Item（249-251 行区域）
- Modify: `ShopKeeper_frontend/src/pages/Supplier.jsx:18` 及电话 Form.Item（248-250 行区域）

**Interfaces:**
- Produces: `validators.js` 导出 `phonePattern: RegExp`，后续任务不再使用它（仅本任务消费）。

- [ ] **Step 1: 创建共享正则模块**

创建 `ShopKeeper_frontend/src/utils/validators.js`：

```js
// 联系电话：手机号（11位）或固话（区号+号码，横线可选）
export const phonePattern = /^(1[3-9]\d{9}|0\d{2,3}-?\d{7,8})$/
```

- [ ] **Step 2: Customer.jsx 接入**

在 `import * as customerApi from '../api/customerApi'` 下一行加：

```js
import { phonePattern } from '../utils/validators'
```

把弹窗内电话 Form.Item 改为（仅加 rules，其余不动）：

```jsx
              <Form.Item
                name="phone"
                label="联系电话"
                rules={[{ pattern: phonePattern, message: '请输入11位手机号或带区号的固话' }]}
              >
                <Input maxLength={20} />
              </Form.Item>
```

- [ ] **Step 3: Supplier.jsx 接入**

同样在 `import * as supplierApi from '../api/supplierApi'` 下一行加 import：

```js
import { phonePattern } from '../utils/validators'
```

弹窗内电话 Form.Item 做与 Step 2 完全相同的改造（rules + message 文案一致）。

- [ ] **Step 4: 构建验证与手工核对**

Run（在 `ShopKeeper_frontend/`）：`npm run build`
Expected: 构建成功，无 eslint 报错。

手工核对（前端 dev server 已启动时）：客户管理 → 新增客户 → 电话填 `123456` → 点保存，表单字段下方红字「请输入11位手机号或带区号的固话」且不发请求；改填 `13800138000` 可保存。供应商页同样验证。

- [ ] **Step 5: 提交**

```bash
git add ShopKeeper_frontend/src/utils/validators.js \
        ShopKeeper_frontend/src/pages/Customer.jsx \
        ShopKeeper_frontend/src/pages/Supplier.jsx
git commit -m "feat: 客户/供应商电话表单格式校验（前端）"
```

---

### Task 3: sys_user 增加 role 字段（SQL + 实体 + Mapper）

**Files:**
- Modify: `sql/init.sql:19-30`
- Modify: `sql/test-data.sql:24-27`
- Create: `sql/v2_add_role.sql`
- Modify: `ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/entity/SysUser.java`
- Modify: `ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/mapper/SysUserMapper.java:13-21`
- Modify: `ShopKeeper_backend/src/main/resources/mapper/SysUserMapper.xml`
- Modify: `ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/service/UserService.java:21-28`
- Modify: `ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/controller/UserController.java:29-35`

**Interfaces:**
- Produces: `SysUser.getRole(): Integer`（1 店长 / 0 店员）；`UserService.page(String username, String realName, Integer role, Integer status, int page, int size)`；Mapper 的 count/selectPage 增加 `@Param("role") Integer role`。Task 4/5 拦截器依赖 `SysUser.getRole()`，Task 8 用户页依赖 role 查询。

- [ ] **Step 1: 更新建表脚本**

`sql/init.sql` 中 sys_user 建表语句，在 `gender CHAR(1) ...` 行之后、`status TINYINT ...` 行之前插入：

```sql
    role       TINYINT      NOT NULL DEFAULT 0 COMMENT '1店长 0店员',
```

在 `CONSTRAINT ck_user_gender CHECK (gender IN ('M','F')),` 行之后加一行：

```sql
    CONSTRAINT ck_user_role CHECK (role IN (0,1)),
```

- [ ] **Step 2: 更新种子数据并新建升级脚本**

`sql/test-data.sql` 第 24-27 行整段替换为（密码哈希保持原值不动）：

```sql
INSERT INTO sys_user (user_id, username, password, real_name, gender, role, status) VALUES
(1, 'admin',    '$2a$10$h/rhDQCy34lHe.ApfxhDNuHKW.AhBaI484YVe4VD5F8YwYpAj5O3y', '系统管理员', 'M', 1, 1),
(2, 'zhangwei', '$2a$10$h/rhDQCy34lHe.ApfxhDNuHKW.AhBaI484YVe4VD5F8YwYpAj5O3y', '张伟',       'M', 0, 1),
(3, 'lina',     '$2a$10$h/rhDQCy34lHe.ApfxhDNuHKW.AhBaI484YVe4VD5F8YwYpAj5O3y', '李娜',       'F', 0, 1);
```

创建 `sql/v2_add_role.sql`：

```sql
-- -------------------------------------------------------------
-- v2 升级：sys_user 增加角色字段（店长 1 / 店员 0）
-- 适用于已按 init.sql 初始化过的存量数据库
-- -------------------------------------------------------------
ALTER TABLE sys_user
    ADD COLUMN role TINYINT NOT NULL DEFAULT 0 COMMENT '1店长 0店员' AFTER gender,
    ADD CONSTRAINT ck_user_role CHECK (role IN (0,1));

-- 存量账号：admin 设为店长，其余账号默认店员
UPDATE sys_user SET role = 1 WHERE username = 'admin';
```

- [ ] **Step 3: 请用户执行升级脚本**

提示用户在 MySQL 中对 `jxc_db` 执行 `sql/v2_add_role.sql`（若计划重建库则执行更新后的 init.sql + test-data.sql）。等待用户确认成功后继续——后续 Task 5 的 curl 验收依赖该字段。

- [ ] **Step 4: 实体加 role**

`SysUser.java` 在 `gender` 字段与 `status` 字段之间加：

```java
    /** 1 店长 / 0 店员 */
    private Integer role;
```

- [ ] **Step 5: Mapper 接口加 role 参数**

`SysUserMapper.java` 的 `count` 与 `selectPage` 方法签名改为：

```java
    long count(@Param("username") String username,
               @Param("realName") String realName,
               @Param("role") Integer role,
               @Param("status") Integer status);

    List<SysUser> selectPage(@Param("username") String username,
                             @Param("realName") String realName,
                             @Param("role") Integer role,
                             @Param("status") Integer status,
                             @Param("offset") int offset,
                             @Param("size") int size);
```

- [ ] **Step 6: Mapper XML 改造**

`SysUserMapper.xml` 做四处修改：

① `baseColumns` 改为：

```xml
    <sql id="baseColumns">
        user_id, username, real_name, gender, role, status
    </sql>
```

② `selectByUsername` 的列名行改为：

```xml
        SELECT user_id, username, password, real_name, gender, role, status
```

③ `count` 的 `<where>` 内、`realName` 条件之后加：

```xml
            <if test="role != null">
                AND role = #{role}
            </if>
```

④ `selectPage` 的 `<where>` 内同样位置加同一个 role 条件块。

⑤ `insert` 整条改为：

```xml
    <insert id="insert" parameterType="org.example.shopkeeper_backend.entity.SysUser"
            useGeneratedKeys="true" keyProperty="userId">
        INSERT INTO sys_user (username, password, real_name, gender, role, status)
        VALUES (#{username}, #{password}, #{realName}, #{gender}, #{role}, #{status})
    </insert>
```

⑥ `update` 的 `<set>` 中在 gender 行之后加：

```xml
            <if test="role != null">role = #{role},</if>
```

- [ ] **Step 7: UserService 与 UserController 透传 role**

`UserService.java` 的 `page` 方法改为：

```java
    public PageResult<SysUser> page(String username, String realName, Integer role, Integer status,
                                    int page, int size) {
        long total = userMapper.count(username, realName, role, status);
        List<SysUser> records = total == 0
                ? List.of()
                : userMapper.selectPage(username, realName, role, status, (page - 1) * size, size);
        return new PageResult<>(total, records);
    }
```

`UserController.java` 的 page 方法加参数并透传：

```java
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
```

- [ ] **Step 8: 编译并提交**

Run：`mvn -q compile`
Expected: BUILD SUCCESS。

```bash
git add sql/init.sql sql/test-data.sql sql/v2_add_role.sql \
        ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/entity/SysUser.java \
        ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/mapper/SysUserMapper.java \
        ShopKeeper_backend/src/main/resources/mapper/SysUserMapper.xml \
        ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/service/UserService.java \
        ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/controller/UserController.java
git commit -m "feat: sys_user 增加 role 角色字段及角色查询条件"
```

---

### Task 4: @OwnerOnly 注解 + OwnerInterceptor

**Files:**
- Create: `ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/common/OwnerOnly.java`
- Create: `ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/interceptor/OwnerInterceptor.java`
- Modify: `ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/config/WebConfig.java`
- Test: `ShopKeeper_backend/src/test/java/org/example/shopkeeper_backend/interceptor/OwnerInterceptorTest.java`

**Interfaces:**
- Consumes: `LoginInterceptor.SESSION_USER`（session key）、session 中的 `SysUser`（Task 3 起带 role）。
- Produces: 注解 `@OwnerOnly`（`@Target({TYPE,METHOD})`、`RUNTIME`）；拦截器按「方法注解 → 类注解」顺序检查，无注解放行，role=1 放行，否则 403。Task 5 用注解标注端点。

- [ ] **Step 1: 写失败测试**

创建 `OwnerInterceptorTest.java`：

```java
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
```

- [ ] **Step 2: 运行测试确认失败**

Run：`mvn -Dtest=OwnerInterceptorTest test`
Expected: 编译失败，「找不到符号 OwnerOnly」。

- [ ] **Step 3: 创建注解与拦截器**

创建 `OwnerOnly.java`：

```java
package org.example.shopkeeper_backend.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 仅店长（role=1）可访问；可标在类或方法上，方法注解优先于类注解
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OwnerOnly {
}
```

创建 `OwnerInterceptor.java`：

```java
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
```

- [ ] **Step 4: 运行测试通过**

Run：`mvn -Dtest=OwnerInterceptorTest test`
Expected: 7 个测试全 PASS。

- [ ] **Step 5: 注册到 WebConfig**

`WebConfig.java` 改造完整文件相关两处：

import 区加：

```java
import org.example.shopkeeper_backend.interceptor.OwnerInterceptor;
```

字段区加：

```java
    @Autowired
    private OwnerInterceptor ownerInterceptor;
```

`addInterceptors` 改为：

```java
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
        registry.addInterceptor(ownerInterceptor)
                .addPathPatterns("/api/**");
    }
```

- [ ] **Step 6: 编译并提交**

Run：`mvn -q compile`，Expected: BUILD SUCCESS。

```bash
git add ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/common/OwnerOnly.java \
        ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/interceptor/OwnerInterceptor.java \
        ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/config/WebConfig.java \
        ShopKeeper_backend/src/test/java/org/example/shopkeeper_backend/interceptor/OwnerInterceptorTest.java
git commit -m "feat: @OwnerOnly 注解与店长权限拦截器"
```

---

### Task 5: 后端受保护端点标注与端到端验收

**Files:**
- Modify: `ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/controller/UserController.java`（类级）
- Modify: `PurchaseController.java:64-66`、`SaleController.java:64-66`（audit 方法）
- Modify: `PurchaseController.java:57-59`、`SaleController.java:57-59`、`CustomerController.java:61`、`SupplierController.java:61`、`ProductController.java:61`（delete 方法）

**Interfaces:**
- Consumes: Task 4 的 `@OwnerOnly`；Task 3 的数据库 role 数据。
- Produces: 无新接口；既有端点行为按角色分化。Task 7 前端按同一分化呈现。

- [ ] **Step 1: UserController 类级标注**

`UserController.java` import 区加：

```java
import org.example.shopkeeper_backend.common.OwnerOnly;
```

在 `public class UserController {` 的上一行加：

```java
@OwnerOnly
```

- [ ] **Step 2: 采购/销售审核方法标注**

`PurchaseController.java` 与 `SaleController.java` 各加同一条 import，并在各自 `@PutMapping("/{...}/audit")` 的上一行、`/** 审核 */` 注释之下加 `@OwnerOnly`。PurchaseController 结果为：

```java
    /** 审核 */
    @OwnerOnly
    @PutMapping("/{poNo}/audit")
    public Result<Void> audit(@PathVariable String poNo) {
```

SaleController 对称（路径变量 soNo）。

- [ ] **Step 3: 五个 delete 方法标注**

在 `CustomerController`、`SupplierController`、`ProductController`、`PurchaseController`、`SaleController` 各加 `import org.example.shopkeeper_backend.common.OwnerOnly;`，并在每个 delete 映射方法上加注解。以 CustomerController 为例：

```java
    @OwnerOnly
    @DeleteMapping("/{customerId}")
```

SupplierController（`/{supplierId}`）、ProductController（`/{productId}`）、PurchaseController（`/{poNo}`）、SaleController（`/{soNo}`）同样标注。

- [ ] **Step 4: 编译并重启后端**

Run：`mvn -q compile`，Expected: BUILD SUCCESS。重启 Spring Boot 应用（端口 8080）。

- [ ] **Step 5: curl 端到端验收**

在一个可写临时目录（如 `D:/tmp`）依次执行。店员登录（zhangwei，密码按 test-data 实际密码；若不知密码，用 admin 登录后在用户管理给 zhangwei 重置，或直接用两个已知账号）：

```bash
# 店员登录，保存 cookie
curl -i -c clerk.txt -H "Content-Type: application/json" \
  -d '{"username":"zhangwei","password":"123456"}' \
  http://localhost:8080/api/auth/login
```

Expected: HTTP/1.1 200，响应体含 role:0（Review Focus 第 5 条：登录不受影响）。若密码不对，先以 admin 登录重置后重试。

店员会话执行下列请求，全部 Expected **403**：

```bash
# 用户管理：GET / POST / PUT（Review Focus 第 1 条：类级覆盖全部方法）
curl -i -b clerk.txt http://localhost:8080/api/users
curl -i -b clerk.txt -H "Content-Type: application/json" -d '{"username":"x","password":"x","realName":"x","gender":"M","role":0,"status":1}' http://localhost:8080/api/users
curl -i -b clerk.txt -H "Content-Type: application/json" -d '{"userId":2,"realName":"张伟2"}' -X PUT http://localhost:8080/api/users

# 单据审核（用库中实际存在的未审核单号替换 CG...）
curl -i -b clerk.txt -X PUT http://localhost:8080/api/purchases/CG替换成真实单号/audit
curl -i -b clerk.txt -X PUT http://localhost:8080/api/sales/XS替换成真实单号/audit

# 删除
curl -i -b clerk.txt -X DELETE http://localhost:8080/api/customers/KH9999
curl -i -b clerk.txt -X DELETE http://localhost:8080/api/suppliers/GYS999
curl -i -b clerk.txt -X DELETE http://localhost:8080/api/products/SP9999
curl -i -b clerk.txt -X DELETE http://localhost:8080/api/purchases/CG替换成真实未审核单号
curl -i -b clerk.txt -X DELETE http://localhost:8080/api/sales/XS替换成真实未审核单号
```

店员会话下列请求 Expected **200**（店员保留能力）：

```bash
curl -i -b clerk.txt http://localhost:8080/api/purchases/CG替换成真实单号   # 详情 GET
curl -i -b clerk.txt http://localhost:8080/api/stock
```

店长登录与通过验证：

```bash
curl -i -c owner.txt -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"123456"}' \
  http://localhost:8080/api/auth/login
curl -i -b owner.txt http://localhost:8080/api/users
```

Expected: 登录 200 且 role:1；用户列表 200。审核一个真实未审核采购单，Expected 200；**对同一单号再调一次 audit，Expected 业务错误「该单据已审核」（HTTP 200 包装的失败 Result 或业务错误码，message 为「该单据已审核」——Review Focus 第 3 条）**。后续前端任务如需未审核单据，可重新建单。

清理：`rm -f clerk.txt owner.txt`。

- [ ] **Step 6: 提交**

```bash
git add ShopKeeper_backend/src/main/java/org/example/shopkeeper_backend/controller/
git commit -m "feat: 用户管理、单据审核与删除操作限定店长权限"
```

---

### Task 6: 审核 / 查看详情页（编辑页三模式 + 列表入口）

**Files:**
- Modify: `ShopKeeper_frontend/src/pages/PurchaseEdit.jsx`
- Modify: `ShopKeeper_frontend/src/pages/SaleEdit.jsx`
- Modify: `ShopKeeper_frontend/src/App.jsx`
- Modify: `ShopKeeper_frontend/src/pages/Purchase.jsx:80-84` 及操作列（114-156 行区域）
- Modify: `ShopKeeper_frontend/src/pages/Sale.jsx:80-84` 及操作列（114-156 行区域）

**Interfaces:**
- Consumes: `purchaseApi.audit(poNo)`、`saleApi.audit(soNo)`（已存在，PUT 方法）；`useAuth().user.role`。
- Produces: 路由 `/purchase/audit/:poNo`、`/purchase/view/:poNo`、`/sale/audit/:soNo`、`/sale/view/:soNo`。Task 7 不再改这两个编辑页。

- [ ] **Step 1: PurchaseEdit —— 模式判定**

`PurchaseEdit.jsx` 三处头部改动：

① router import 改为：

```jsx
import { useNavigate, useParams, useLocation } from 'react-router-dom'
```

② 在 `import * as productApi from '../api/productApi'` 下一行加：

```jsx
import { useAuth } from '../context/AuthContext'
```

③ 组件开头一段替换为：

```jsx
export default function PurchaseEdit() {
  const navigate = useNavigate()
  const location = useLocation()
  const { poNo } = useParams()
  const { user: authUser } = useAuth()

  // edit 可编辑；audit 只读且店长可审核；view 纯只读
  const mode = location.pathname.includes('/audit/')
    ? 'audit'
    : location.pathname.includes('/view/')
      ? 'view'
      : 'edit'
  const readOnly = mode !== 'edit'
  const hasNo = Boolean(poNo)

  const [form] = Form.useForm()
  const [loading, setLoading] = useState(hasNo)
  const [saving, setSaving] = useState(false)
  const [auditing, setAuditing] = useState(false)
  const [suppliers, setSuppliers] = useState([])
  const [products, setProducts] = useState([])
  const [items, setItems] = useState(() => [newRow()])
```

- [ ] **Step 2: PurchaseEdit —— 加载与审核逻辑**

详情加载 effect 的首行由 `if (!isEdit) return` 改为：

```jsx
    if (!hasNo) return
```

依赖数组改为 `[hasNo, poNo, form]`。

在 `handleSave` 之后新增审核处理：

```jsx
  const handleAudit = async () => {
    setAuditing(true)
    try {
      await purchaseApi.audit(poNo)
      message.success('审核成功')
      navigate('/purchase')
    } finally {
      setAuditing(false)
    }
  }
```

`handleSave` 内 payload 的 `poNo: isEdit ? poNo : undefined` 改为 `poNo: hasNo ? poNo : undefined`。

- [ ] **Step 3: PurchaseEdit —— 明细列只读化**

把 `const columns = [` 开始到对应 `]` 结束的整段列定义替换为：

```jsx
  const columns = [
    {
      title: '商品',
      width: 260,
      render: (_, __, index) => (
        <Select
          showSearch
          disabled={readOnly}
          optionFilterProp="label"
          style={{ width: '100%' }}
          placeholder="请选择商品"
          value={items[index].productId}
          onChange={(v) => handleProductChange(index, v)}
          options={products.map((p) => ({
            value: p.productId,
            label: `${p.productId} ${p.productName}`
          }))}
        />
      )
    },
    {
      title: '数量',
      width: 110,
      render: (_, __, index) => (
        <InputNumber
          min={1}
          precision={0}
          disabled={readOnly}
          style={{ width: '100%' }}
          value={items[index].quantity}
          onChange={(v) => updateRow(index, { quantity: v })}
        />
      )
    },
    {
      title: '单价(元)',
      width: 130,
      render: (_, __, index) => (
        <InputNumber
          min={0}
          precision={2}
          disabled={readOnly}
          style={{ width: '100%' }}
          value={items[index].unitPrice}
          onChange={(v) => updateRow(index, { unitPrice: v })}
        />
      )
    },
    {
      title: '折扣',
      width: 110,
      render: (_, __, index) => (
        <Select
          style={{ width: '100%' }}
          disabled={readOnly}
          value={items[index].discount}
          onChange={(v) => updateRow(index, { discount: v })}
          options={discountOptions}
        />
      )
    },
    {
      title: '金额(元)',
      width: 120,
      align: 'right',
      render: (_, __, index) => rowAmount(items[index]).toFixed(2)
    }
  ]

  // 只读模式下不提供删行入口
  if (!readOnly) {
    columns.push({
      title: '操作',
      width: 80,
      render: (_, __, index) => (
        <a
          style={{ color: '#ff4d4f' }}
          onClick={() => setItems(items.filter((_, i) => i !== index))}
        >
          删除
        </a>
      )
    })
  }
```

- [ ] **Step 4: PurchaseEdit —— 表单与底部按钮**

四处小改：

① 单号输入框 `value={isEdit ? poNo : '保存后自动生成'}` 改为：

```jsx
                <Input value={hasNo ? poNo : '保存后自动生成'} disabled />
```

② 主表单标签加 disabled：

```jsx
        <Form form={form} layout="vertical" disabled={readOnly}>
```

③ 明细卡的「添加明细」按钮改为只在编辑模式渲染：

```jsx
        extra={
          readOnly
            ? null
            : (
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setItems([...items, newRow()])}
              >
                添加明细
              </Button>
            )
        }
```

④ 底部按钮区整段替换为：

```jsx
      <div>
        <Space>
          <Button icon={<ArrowLeftOutlined />} onClick={() => navigate('/purchase')}>
            返回
          </Button>
          {mode === 'edit' && (
            <Button type="primary" loading={saving} onClick={handleSave}>
              保存单据
            </Button>
          )}
          {mode === 'audit' && authUser?.role === 1 && (
            <Button type="primary" loading={auditing} onClick={handleAudit}>
              审核通过
            </Button>
          )}
        </Space>
      </div>
```

- [ ] **Step 5: SaleEdit 对称改造**

对 `SaleEdit.jsx` 做与 Step 1-4 完全对称的改动，差异点仅为以下四处，其余代码逐字相同：

- `poNo` → `soNo`；`purchaseApi` → `saleApi`；`suppliers` 相关不存在（SaleEdit 是 customers，保持不动）。
- `handleAudit` 内为 `await saleApi.audit(soNo)`、`navigate('/sale')`。
- 返回按钮目标 `navigate('/sale')`；明细缺省行校验文案沿用「销售明细」。
- 模式判定代码完全相同。

- [ ] **Step 6: App.jsx 增加路由**

在现有 `purchase/edit/:poNo` 行之后加两行：

```jsx
          <Route path="purchase/audit/:poNo" element={<PurchaseEdit />} />
          <Route path="purchase/view/:poNo" element={<PurchaseEdit />} />
```

在 `sale/edit/:soNo` 行之后加两行：

```jsx
          <Route path="sale/audit/:soNo" element={<SaleEdit />} />
          <Route path="sale/view/:soNo" element={<SaleEdit />} />
```

- [ ] **Step 7: Purchase.jsx 列表入口改造**

删除组件内的 `handleAudit` 函数（80-84 行整段）。操作列 render 中「编辑/审核」相关的前两段替换为：

```jsx
            {audited ? (
              <a onClick={() => navigate(`/purchase/view/${record.poNo}`)}>查看</a>
            ) : (
              <a onClick={() => navigate(`/purchase/edit/${record.poNo}`)}>编辑</a>
            )}
            {audited ? (
              <span style={{ color: '#bbb' }}>审核</span>
            ) : (
              <a onClick={() => navigate(`/purchase/audit/${record.poNo}`)}>审核</a>
            )}
```

删除 Popconfirm 审核块后，若 `Popconfirm` 仍被删除操作使用则保留 import（本文件删除确认仍在用）。

- [ ] **Step 8: Sale.jsx 列表入口改造**

同样删除 `handleAudit` 函数；操作列做与 Step 7 对称替换：`/sale/view/${record.soNo}`、`/sale/edit/${record.soNo}`、`/sale/audit/${record.soNo}`。

- [ ] **Step 9: 构建与手工验收**

Run：`npm run build`，Expected: 成功无报错。

手工验收（admin 店长账号）：
1. 采购单列表对未审核单点「审核」→ 进入只读详情页，可见供应商、日期、全部商品行与合计金额，底部有「返回 / 审核通过」。
2. 点「审核通过」→ 提示审核成功、回到列表，状态变已审核（库存相应增加）。
3. 已审核行操作列为「查看」，进入纯只读页，底部只有「返回」。
4. 销售单同样验证（审核后库存扣减）。
5. 手工对已审核单据在地址栏改输 `/purchase/audit/<已审核单号>`：页面正常只读展示、无「审核通过」按钮（Review Focus 第 3 条）。

- [ ] **Step 10: 提交**

```bash
git add ShopKeeper_frontend/src/pages/PurchaseEdit.jsx \
        ShopKeeper_frontend/src/pages/SaleEdit.jsx \
        ShopKeeper_frontend/src/App.jsx \
        ShopKeeper_frontend/src/pages/Purchase.jsx \
        ShopKeeper_frontend/src/pages/Sale.jsx
git commit -m "feat: 审核跳转单据详情页，已审核单据提供查看入口"
```

---

### Task 7: 前端权限呈现（菜单 / 路由守卫 / 按钮灰化）

**Files:**
- Modify: `ShopKeeper_frontend/src/layouts/MainLayout.jsx:18-53,113-120`
- Modify: `ShopKeeper_frontend/src/App.jsx`
- Modify: `ShopKeeper_frontend/src/pages/Customer.jsx`（操作列删除）
- Modify: `ShopKeeper_frontend/src/pages/Supplier.jsx`（操作列删除）
- Modify: `ShopKeeper_frontend/src/pages/Product.jsx`（操作列删除）
- Modify: `ShopKeeper_frontend/src/pages/Purchase.jsx`（审核 + 删除）
- Modify: `ShopKeeper_frontend/src/pages/Sale.jsx`（审核 + 删除）

**Interfaces:**
- Consumes: `useAuth().user.role`；Task 6 之后列表页的当前代码状态。
- Produces: App.jsx 内 `RequireOwner` 组件（本任务内使用）。

- [ ] **Step 1: MainLayout 菜单按角色过滤**

`MainLayout.jsx` 在组件内（`const { user, loading, logout } = useAuth()` 之后）加派生菜单：

```jsx
  // 店员看不到用户管理
  const visibleMenuItems = menuItems.map((item) => {
    if (!item.children) return item
    const children = item.children.filter(
      (child) => child.key !== '/user' || user.role === 1
    )
    return { ...item, children }
  })
```

把 `<Menu ... items={menuItems}` 改为 `items={visibleMenuItems}`。

- [ ] **Step 2: MainLayout 芯片显示角色**

把 `<div className="sk-user-role">系统用户</div>` 替换为：

```jsx
                <div className="sk-user-role">{user.role === 1 ? '店长' : '店员'}</div>
```

- [ ] **Step 3: App.jsx 路由守卫**

`App.jsx` import 区加：

```jsx
import { useAuth } from './context/AuthContext'
```

在 `export default function App() {` 之前定义：

```jsx
function RequireOwner({ children }) {
  const { user } = useAuth()
  if (user?.role !== 1) return <Navigate to="/stock" replace />
  return children
}
```

把用户路由改为：

```jsx
          <Route
            path="user"
            element={(
              <RequireOwner>
                <User />
              </RequireOwner>
            )}
          />
```

注意 RequireOwner 渲染于 MainLayout 内，MainLayout 已处理 loading 与未登录，故此处 user 必已存在。

- [ ] **Step 4: Customer / Supplier / Product 删除按钮灰化**

三个文件操作列中的删除入口做同一改造。以 Customer.jsx 为例，组件内取角色：

```jsx
import { useAuth } from '../context/AuthContext'
```

```jsx
  const { user } = useAuth()
```

操作列里把 `<Popconfirm ...>删除</Popconfirm>` 整段替换为条件渲染：

```jsx
          {user.role === 1 ? (
            <Popconfirm
              title="删除确认"
              description={`确定删除客户「${record.customerName}」吗？`}
              okText="确定删除"
              cancelText="取消"
              okButtonProps={{ danger: true }}
              onConfirm={() => handleDelete(record.customerId)}
            >
              <a style={{ color: 'var(--cinnabar)' }}>删除</a>
            </Popconfirm>
          ) : (
            <span style={{ color: '#bbb' }}>删除</span>
          )}
```

Supplier.jsx（名称 supplierName、id supplierId）、Product.jsx 按各文件原有 Popconfirm 内容对称处理。

- [ ] **Step 5: Purchase.jsx / Sale.jsx 审核与删除灰化**

Purchase.jsx 加 `useAuth` 导入与 `const { user } = useAuth()`。操作列中「审核」链接（Task 6 Step 7 的产物）改为：

```jsx
            {audited ? (
              <span style={{ color: '#bbb' }}>审核</span>
            ) : user.role === 1 ? (
              <a onClick={() => navigate(`/purchase/audit/${record.poNo}`)}>审核</a>
            ) : (
              <span style={{ color: '#bbb' }}>审核</span>
            )}
```

删除 Popconfirm 做与 Step 4 相同的条件灰化（店员显示灰字「删除」）。

Sale.jsx 对称处理（soNo、`/sale/audit/`、客户名称）。

- [ ] **Step 6: 构建与手工验收**

Run：`npm run build`，Expected: 成功。

用 zhangwei（店员）登录验收（Review Focus 第 4 条）：
1. 左侧「基础档案」下没有「用户管理」；地址栏输 `/user` 回车 → 自动跳回 `/stock`。
2. 各列表「删除」均为灰色不可点；采购/销售列表「审核」灰色不可点；「新增」「编辑」正常可用。
3. 地址栏输 `/purchase/audit/<真实未审核单号>` → 只读详情正常显示，底部只有「返回」，无审核按钮。
4. 右上角芯片显示「店员」。
5. 退出换 admin 登录：菜单与按钮全部恢复，芯片显示「店长」。

- [ ] **Step 7: 提交**

```bash
git add ShopKeeper_frontend/src/layouts/MainLayout.jsx \
        ShopKeeper_frontend/src/App.jsx \
        ShopKeeper_frontend/src/pages/Customer.jsx \
        ShopKeeper_frontend/src/pages/Supplier.jsx \
        ShopKeeper_frontend/src/pages/Product.jsx \
        ShopKeeper_frontend/src/pages/Purchase.jsx \
        ShopKeeper_frontend/src/pages/Sale.jsx
git commit -m "feat: 菜单、路由与操作按钮按店长/店员角色呈现"
```

---

### Task 8: 用户管理页角色支持（列 / 表单 / 查询）

**Files:**
- Modify: `ShopKeeper_frontend/src/pages/User.jsx`

**Interfaces:**
- Consumes: Task 3 的后端 role 字段与 `/api/users?role=` 查询；店员根本进不了此页（Task 7），无需在页内再判角色。
- Produces: 无。

- [ ] **Step 1: 查询区增加角色条件**

`emptyQuery` 改为 `{ username: '', realName: '', role: undefined, status: undefined }`。

在查询区「状态」项之前插入一个角色下拉 Col（宽度按现有布局协调，可与状态并列，span 自当前布局中取 4）：

```jsx
          <Col span={4}>
            <Form.Item label="角色">
              <Select
                allowClear
                placeholder="全部"
                value={query.role}
                onChange={(v) => setQuery({ ...query, role: v })}
                options={[
                  { value: 1, label: '店长' },
                  { value: 0, label: '店员' }
                ]}
              />
            </Form.Item>
          </Col>
```

查询提交时 `userApi.page({ ...applied, page, size })` 已会透传 role，无需改 loadData。检查查询/重置对新字段的行为与现有字段一致（handleSearch、handleReset 用整体对象，已覆盖）。

- [ ] **Step 2: 表格增加角色列**

在「真实姓名」列之后加：

```jsx
    {
      title: '角色',
      dataIndex: 'role',
      width: 90,
      render: (r) => (r === 1 ? <Tag color="gold">店长</Tag> : <Tag>店员</Tag>)
    },
```

- [ ] **Step 3: 表单增加角色项**

`openAdd` 默认值加 role：`form.setFieldsValue({ gender: 'M', role: 0, status: 1 })`。

在性别所在 Row 之后、状态项之前加：

```jsx
          <Form.Item name="role" label="角色" rules={[{ required: true, message: '请选择角色' }]}>
            <Radio.Group>
              <Radio value={1}>店长</Radio>
              <Radio value={0}>店员</Radio>
            </Radio.Group>
          </Form.Item>
```

- [ ] **Step 4: 构建与手工验收**

Run：`npm run build`，Expected: 成功。

admin 登录验收：用户列表角色列正确（admin 金色「店长」，其余「店员」）；按角色查询结果正确；新增用户时角色必选、默认店员；把一个店员编辑为店长后，该账号重新登录可见用户管理菜单。

- [ ] **Step 5: 提交**

```bash
git add ShopKeeper_frontend/src/pages/User.jsx
git commit -m "feat: 用户管理支持角色展示、编辑与条件查询"
```

---

### Task 9: 设计文档与 ER 图同步

**Files:**
- Modify: `docs/01-ER模型设计.md`
- Modify: `docs/02-数据库设计.md`
- Modify: `docs/03-ER图Mermaid代码.md`

**Interfaces:**
- Consumes: Task 3 的最终表结构。
- Produces: 文档与代码一致，可作为课程交付物。

- [ ] **Step 1: 01-ER模型设计.md 同步**

五处修改：

① 第 19 行业务边界「不在本系统范围内：…多角色权限。」改为：

```markdown
不在本系统范围内：收付款、退货、仓库/库位管理、会员积分、两级以上的多角色权限（本系统仅设店长/店员两级）。
```

② E1 用户属性表（71-80 行区域）表格末尾加一行，且原状态序号 6 保持不变、新增序号 7：

```markdown
| 7 | 角色 | 人员在系统中的角色 | 枚举：{店长，店员} | |
```

③ 第 82 行「共 **6 个椭圆**。」改为「共 **7 个椭圆**。」

④ 核对表中属性计数三处更新：第 234 行 `41 个（实体属性 35 + 联系属性 6）` 改为 `42 个（实体属性 36 + 联系属性 6）`；第 236 行 `41 条` 改为 `42 条`；第 323 行 `椭圆合计 | 41` 改为 `42`；第 342 行「椭圆总数 41」改为「椭圆总数 42」。

- [ ] **Step 2: 02-数据库设计.md 同步**

① §3.1 sys_user 字段表，在 gender 行与 status 行之间插入：

```markdown
| role | TINYINT | 否 | 0 | CHECK(0,1) | 1 店长 / 0 店员 | **单选按钮组** |
```

② §完整性约束（约 170 行）CHECK 清单一句中加入角色取值：

```markdown
| 用户自定义完整性 | CHECK：性别 M/F；角色 1/0；状态取值；数量 > 0；单价 ≥ 0；折扣 0~1 |
```

- [ ] **Step 3: 03-ER图Mermaid代码.md 同步**

四处修改：

① 第 6 行与第 51 行「全 41 属性 / 41 个属性」改为 42。

② 图二 E1 注释 `%% ===== E1 用户（6 属性） =====` 改为 `（7 属性）`，并在 `U6(["状态"]) --- U` 之后加：

```mermaid
      U7(["角色"]) --- U
```

③ 图二底部 class 行（第 154 行）`class U1,U2,U3,U4,U5,U6 attr` 改为 `class U1,U2,U3,U4,U5,U6,U7 attr`。

④ 第 170 行核对句「实体属性 35、联系属性 6、椭圆合计 41」改为「实体属性 36、联系属性 6、椭圆合计 42」。

- [ ] **Step 4: 通读核对并提交**

通读三份文档确认无遗漏的属性计数或「多角色权限」旧表述（可搜索 `41`、`6 属性`、`多角色` 复核）。

```bash
git add docs/01-ER模型设计.md docs/02-数据库设计.md docs/03-ER图Mermaid代码.md
git commit -m "docs: ER 模型与数据库设计同步角色属性（42 属性）"
```

---

## 完成判据（全部任务后整体回归）

1. 后端 `mvn -q compile` 通过；`mvn -Dtest=PhoneValidatorTest,OwnerInterceptorTest test` 全绿。
2. 前端 `npm run build` 通过。
3. admin（店长）：三类档案与两张单据的增删改查、审核、用户管理全部可用；审核必经详情页。
4. zhangwei（店员）：无用户管理菜单与路由；无删除、审核入口；新增/编辑/查看正常；任何越权请求被后端 403 拒绝。
5. 电话录入非法值在前端被拦、后端兜底；合法手机与固话均可保存。
6. sql 三份脚本与 docs 三份文档同当前实现一致。
