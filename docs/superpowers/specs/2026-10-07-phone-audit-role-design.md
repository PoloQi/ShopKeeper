# 电话校验、审核详情页与两级权限设计

- 日期：2026-10-07
- 项目：店管家进销存管理系统（ShopKeeper）
- 状态：设计已获用户口头批准，待书面 spec 审阅

## 1. 背景与目标

当前系统存在三个问题：

1. **客户（及供应商）联系电话无格式校验**：录入 `123456` 这样的号码也能保存，无法保证档案数据质量。
2. **审核操作只弹确认框**：采购单/销售单列表点击「审核」直接弹 `Popconfirm`「是否确认审核」，审核人看不到单据明细（商品、数量、金额）就可能通过，审核形同虚设。
3. **没有角色区分**：`sys_user` 表无角色字段，任何登录用户都能审核单据、管理用户账号、删除数据，存在越权风险。

目标：电话录入前后端双重校验；审核前必须进入单据详情页查看完整信息；引入「店长 / 店员」两级角色，敏感操作仅店长可执行。

## 2. 已确认的决策

| 决策点 | 结论 |
|---|---|
| 角色命名 | **店长（role=1）/ 店员（role=0）** |
| 仅店长可执行 | ①单据审核 ②用户管理（整个菜单） ③删除数据 |
| 店员保留能力 | 登录、查看所有页面、新增/编辑基础档案、新增/编辑未审核单据 |
| 电话规则 | 手机号或固话均可；非必填，填写才校验 |
| 校验范围 | 客户管理、供应商管理两处电话字段 |

电话正则（整体一个正则，两个分支）：

```
^(1[3-9]\d{9}|0\d{2,3}-?\d{7,8})$
```

- 手机号：`1` 开头、第二位 `3-9`、共 11 位，如 `13800138000`
- 固话：`0` 开头的 3-4 位区号 + 可选短横线 + 7-8 位号码，如 `02012345678`、`020-12345678`、`0755-1234567`

## 3. 详细设计

### 3.1 电话格式校验

#### 后端

新增 `common/PhoneValidator.java`：

```java
package org.example.shopkeeper_backend.common;

import java.util.regex.Pattern;

/** 联系电话校验：手机号或固话；null/空白视为不填，跳过校验 */
public final class PhoneValidator {

    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(1[3-9]\\d{9}|0\\d{2,3}-?\\d{7,8})$");

    private PhoneValidator() {}

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

在 `CustomerService` 和 `SupplierService` 的 `add`、`update` 方法首行调用 `PhoneValidator.check(customer.getPhone())`（Supplier 对应同名方法）。校验不合法抛 `BusinessException`，由现有 `GlobalExceptionHandler` 统一返回错误信息。

#### 前端

新增 `src/utils/validators.js`：

```js
// 手机号（11位）或固话（区号+号码，横线可选）
export const phonePattern = /^(1[3-9]\d{9}|0\d{2,3}-?\d{7,8})$/
```

`Customer.jsx`、`Supplier.jsx` 弹窗中的电话 `Form.Item` 增加规则：

```jsx
<Form.Item
  name="phone"
  label="联系电话"
  rules={[{ pattern: phonePattern, message: '请输入11位手机号或带区号的固话' }]}
>
  <Input maxLength={20} />
</Form.Item>
```

查询区的电话输入框不校验（查询条件允许片段匹配）。

### 3.2 审核改为跳转详情页

#### 编辑页改造为三种模式

`PurchaseEdit.jsx`、`SaleEdit.jsx` 共用同一改造方案。通过当前 URL 判定模式：

```js
const mode = location.pathname.includes('/audit/')
  ? 'audit'
  : location.pathname.includes('/view/')
    ? 'view'
    : 'edit'
```

| 模式 | 路由 | 表单 | 明细行 | 底部按钮 |
|---|---|---|---|---|
| edit（现状） | `/purchase/edit/:poNo`、`/purchase/new` | 可编辑 | 可增删改 | 返回、保存单据 |
| audit | `/purchase/audit/:poNo` | 只读 | 只读，无增删 | 返回、**审核通过**（仅店长可见此按钮） |
| view | `/purchase/view/:poNo` | 只读 | 只读，无增删 | 仅返回 |

只读实现要点：

- 头部 `Form` 加 `disabled` 属性（Ant Design 会自动禁用内部所有控件）。
- 明细表由独立 `items` state 驱动、不在 `Form` 内：商品 `Select`、数量/单价 `InputNumber`、折扣 `Select` 在 `mode !== 'edit'` 时设 `disabled`；隐藏「添加明细」按钮和每行「删除」。
- 「审核通过」按钮点击后**直接调用审核接口，不再弹确认框**（完整单据信息已在页面展示，弹窗确认已无必要）。成功后 `message.success('审核成功')` 并 `navigate('/purchase')`（销售为 `/sale`）。
- audit 模式下，`useAuth()` 当前用户 `role !== 1` 时不渲染「审核通过」按钮（店员手输 URL 进来只能看，不能审；即便绕过，后端仍返回 403）。

数据加载沿用现有 `purchaseApi.getDetail` / `saleApi.getDetail`（详情接口不加角色限制，登录即可访问）。现有加载逻辑以 `const isEdit = Boolean(poNo)` 为条件，改造后语义变为「路由带单据编号参数即为单据页（edit/audit/view 三种都需要加载详情），仅 `/new` 不加载」，变量语义随模式拆分调整，`Spin` 初始加载态同样对 audit/view 生效。

#### 路由（App.jsx）

新增 4 条：

```jsx
<Route path="purchase/audit/:poNo" element={<PurchaseEdit />} />
<Route path="purchase/view/:poNo" element={<PurchaseEdit />} />
<Route path="sale/audit/:soNo" element={<SaleEdit />} />
<Route path="sale/view/:soNo" element={<SaleEdit />} />
```

#### 列表页改动（Purchase.jsx / Sale.jsx）

- 未审核行的「审核」：删除 `Popconfirm`，改为 `<a onClick={() => navigate('/purchase/audit/' + record.poNo)}>审核</a>`。
- 已审核行：原灰色不可点的「编辑」改为可点击的「查看」→ 跳 `/purchase/view/:poNo`，使已审核单据有详情入口。
- 未审核行的「编辑」入口保持不变。

### 3.3 两级权限（店长 / 店员）

#### 数据库变更（SQL 由用户自行执行）

**`sql/init.sql`** —— `sys_user` 建表语句在 `gender` 之后增加：

```sql
role       TINYINT      NOT NULL DEFAULT 0 COMMENT '1店长 0店员',
```

并在约束区增加：

```sql
CONSTRAINT ck_user_role CHECK (role IN (0,1))
```

**`sql/test-data.sql`** —— 用户种子数据 INSERT 列表增加 `role`：admin 为 `1`，zhangwei、lina 为 `0`（密码哈希沿用原值，不在此重复）：

```sql
INSERT INTO sys_user (user_id, username, password, real_name, gender, role, status) VALUES
(1, 'admin',    '<原BCrypt哈希不变>', '系统管理员', 'M', 1, 1),
(2, 'zhangwei', '<原BCrypt哈希不变>', '张伟',       'M', 0, 1),
(3, 'lina',     '<原BCrypt哈希不变>', '李娜',       'F', 0, 1);
```

**新增 `sql/v2_add_role.sql`**（存量库升级脚本）：

```sql
-- v2 升级：sys_user 增加角色字段（店长 1 / 店员 0）
ALTER TABLE sys_user
    ADD COLUMN role TINYINT NOT NULL DEFAULT 0 COMMENT '1店长 0店员' AFTER gender,
    ADD CONSTRAINT ck_user_role CHECK (role IN (0,1));

-- 存量账号：admin 设为店长，其余默认店员
UPDATE sys_user SET role = 1 WHERE username = 'admin';
```

#### 后端

1. **实体** `SysUser.java` 增加 `/** 1店长 0店员 */ private Integer role;`
2. **SysUserMapper.xml**：
   - `baseColumns` 改为 `user_id, username, real_name, gender, role, status`
   - `selectByUsername` 列表加 `role`
   - `insert` 列与 VALUES 加 `role`（新用户由表单必选后传入）
   - `update` 的 `<set>` 加 `<if test="role != null">role = #{role},</if>`
   - `count`、`selectPage` 的 `<where>` 各加一个条件 `<if test="role != null">AND role = #{role}</if>`；对应 `SysUserMapper.java` 接口方法签名与 `UserService.page` 增加 `Integer role` 参数
3. **新增注解 `common/OwnerOnly.java`**：

```java
package org.example.shopkeeper_backend.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 标注仅店长（role=1）可访问的接口；可标在类或方法上，方法优先于类 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OwnerOnly {
}
```

4. **新增拦截器 `interceptor/OwnerInterceptor.java`**：

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

/** 店长权限拦截器：@OwnerOnly 标注的类/方法要求 session 用户 role=1，否则 403 */
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

5. **WebConfig 注册**（顺序：先登录、后店长）：

```java
registry.addInterceptor(loginInterceptor)
        .addPathPatterns("/api/**")
        .excludePathPatterns("/api/auth/login");
registry.addInterceptor(ownerInterceptor)
        .addPathPatterns("/api/**");
```

6. **受保护端点清单**：

| 位置 | 标注方式 | 覆盖操作 |
|---|---|---|
| `UserController` | 类级 `@OwnerOnly` | 用户的分页查询、新增、修改、删除、全部用户管理接口 |
| `PurchaseController.audit` | 方法级 | 采购单审核 `PUT /api/purchases/{poNo}/audit` |
| `SaleController.audit` | 方法级 | 销售单审核 `PUT /api/sales/{soNo}/audit` |
| `CustomerController.delete` | 方法级 | 删除客户 |
| `SupplierController.delete` | 方法级 | 删除供应商 |
| `ProductController.delete` | 方法级 | 删除商品 |
| `PurchaseController.delete` | 方法级 | 删除未审核采购单 |
| `SaleController.delete` | 方法级 | 删除未审核销售单 |

注：`UserController` 类级标注后，用户管理的分页查询也仅店长可用，前端菜单与路由已同步隐藏，不产生矛盾。

#### 前端

1. **AuthContext**：无需改动。`/api/auth/info` 返回 session 中的 `SysUser`，实体加 `role` 后自动携带，`user.role` 全前端可用。
2. **MainLayout.jsx**：
   - 菜单按角色过滤：`role !== 1` 时从「基础档案」children 中移除「用户管理」项。
   - 用户芯片副标题 `系统用户` 改为按角色显示 `店长` / `店员`。
3. **App.jsx 路由守卫**：

```jsx
function RequireOwner({ children }) {
  const { user } = useAuth()
  if (user?.role !== 1) return <Navigate to="/stock" replace />
  return children
}
```

`/user` 路由包裹为 `<Route path="user" element={<RequireOwner><User /></RequireOwner>} />`（MainLayout 已保证进入时 `loading` 结束、user 存在）。
4. **列表页按钮（Customer / Supplier / Product / Purchase / Sale）**：
   - 「删除」：`role !== 1` 时渲染灰字占位 `<span style={{ color: '#bbb' }}>删除</span>`，不渲染 `Popconfirm`。
   - 「审核」（采购/销售）：店员同样渲染灰字占位。
   - 新增、编辑入口对店员保留。
5. **User.jsx**：
   - 表格增加「角色」列：店长 `<Tag color="gold">店长</Tag>`、店员 `<Tag>店员</Tag>`。
   - 表单增加角色项（`Radio.Group`：店长/店员，`rules` 必填），新增用户默认 `role: 0`。
   - 查询区增加角色下拉条件（全部/店长/店员）；`userApi.page` 透传 `role` 参数（后端 `UserService` 与 mapper 增加该查询条件）。

### 3.4 设计文档同步

现有 docs 为项目交付物，结构变更必须同步：

- `docs/01-ER模型设计.md`：用户实体增加属性「角色」，属性总数 6 → 7。
- `docs/02-数据库设计.md`：§3.1 sys_user 表结构增加 `role` 行说明；§2 表清单、范式分析结论不受影响（role 为独立单值属性，不改变依赖关系），但需在文中核对后注明。
- `docs/03-ER图Mermaid代码.md`：E1 用户增加角色属性节点（仿照现有 U1/U2 节点命名 U7），图一图二两处均改。
- `sql/init.sql`、`sql/test-data.sql` 按 §3.3 更新，新增 `sql/v2_add_role.sql`。

## 4. 验证计划

### 4.1 后端

1. `mvn compile` 通过；启动后执行 `sql/v2_add_role.sql`。
2. 用 curl/前端分别以 admin（店长）、zhangwei（店员）登录，验证：

| 请求 | 店长 | 店员 |
|---|---|---|
| PUT 采购单/销售单 audit | 200，单据变已审核、库存变动 | 403「需要店长权限」 |
| DELETE 客户/供应商/商品/采购单/销售单 | 200（或既有外键/已审核业务错误） | 403 |
| GET/POST/PUT `/api/users` 全部接口 | 200 | 403 |
| 新增/编辑单据、档案 | 200 | 200 |
| GET 单据详情 | 200 | 200 |

3. 电话校验：对客户/供应商 add、update 分别传 `123456`、`12345678901`（第二位非法）、`abc` → 业务错误提示；`13800138000`、`020-12345678`、留空 → 通过。

### 4.2 前端

1. 店员登录：无「用户管理」菜单；手输 `/user` 被重定向到 `/stock`；各列表无「删除」「审核」可点入口；可正常新增/编辑。
2. 店员手输 `/purchase/audit/<未审核单号>`：能看到完整只读单据，无「审核通过」按钮。
3. 店长登录：全部菜单与入口正常；点「审核」进入详情页，核对商品行与合计后点「审核通过」，返回列表状态变为已审核；已审核单行点「查看」可只读浏览。
4. 客户/供应商弹窗录入 `123456` 保存被表单拦截；合法号码可保存。

## 5. 不在本次范围

- 不引入细粒度按钮级权限表或多角色体系，仅 1/0 两级。
- 不做单据「反审核/弃审」功能。
- 不改造既有外键、视图与库存逻辑。
- 查询区电话输入框不做格式校验（片段查询需要）。
