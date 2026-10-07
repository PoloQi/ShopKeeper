# 店管家 ShopKeeper · 商品进销存管理系统

> 广东工业大学 · 信息管理与信息系统专业《信管专业综合设计》课程项目
>
> 一个面向小型商贸企业的进销存管理系统：管理客户、供应商、商品等基础档案，处理采购入库与销售出库两类业务，并通过数据库视图实时核算库存。

## 项目简介

系统以一次完整的进销存业务为主线：**建立基础档案 → 开具采购单 → 审核入库 → 开具销售单 → 审核出库 → 查询实时库存**。

核心设计遵循数据库设计规范：

- **基本项才入库、组合项拆分、导出项不入库**：金额（单价×数量×折扣）不存储，库存由视图实时计算；
- 采购、销售采用「**单据主表 + 明细从表**」的主从（虚实体）结构，消除重复录入与数据冗余；
- 数据库设计满足 **第三范式（3NF）**。

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Spring Boot 3.3.5、Spring MVC、MyBatis（XML Mapper）、JDK 17 |
| 前端 | React 18、Vite 5、Ant Design 5、React Router 6、Axios |
| 数据库 | MySQL 8（InnoDB，utf8mb4） |
| 鉴权 | Cookie-Session 登录拦截；密码 BCrypt 加密存储 |

## 目录结构

```
管理信息系统/
├── docs/                        # 设计文档
│   ├── 01-ER模型设计.md          # 概念模型设计说明书（含 ER 图绘制手册）
│   ├── 02-数据库设计.md          # 数据库字段级设计、约束、视图、范式分析
│   └── 03-ER图Mermaid代码.md     # ER 图 Mermaid 源码
├── sql/
│   ├── init.sql                 # 建库、8 张表、3 个视图
│   └── test-data.sql            # 测试数据
├── images/                      # ER 图导出图片
├── ShopKeeper_backend/          # Spring Boot 后端（Maven）
└── ShopKeeper_frontend/         # React 前端
```

## 数据库设计

- **8 张表**：用户、客户、供应商、商品、采购单、采购明细、销售单、销售明细；
- **3 个视图**：`v_purchase_full`（采购完整信息）、`v_sale_full`（销售完整信息）、`v_stock`（实时库存）；
- 单据状态：`0 未审核 / 1 已审核`，已审核单据计入库存且禁止修改、删除。

![概念层 ER 结构主图](images/概念层%20ER%20结构主图（精简，不含属性）.png)

完整实体属性与联系基数见 [docs/01-ER模型设计.md](docs/01-ER模型设计.md)。

## 功能模块

- **登录 / 登出**：Session 鉴权，未登录请求统一拦截；
- **基础档案**：客户、供应商、商品、用户的增删改查与多条件组合查询；
- **采购管理**：采购单主从表单（明细动态增删行）、删除确认、审核入库；
- **销售管理**：与采购同构，审核后扣减库存；
- **库存查询**：基于视图 `v_stock` 实时展示累计入库、出库与当前库存。

## 快速开始

### 1. 环境要求

JDK 17+、Maven 3.9+（或使用项目自带 `mvnw`）、Node.js 18+、MySQL 8。

### 2. 初始化数据库

在 MySQL 中依次执行：

```sql
source sql/init.sql;        -- 建库、建表、建视图
source sql/test-data.sql;   -- 导入测试数据
```

### 3. 启动后端

后端默认连接 `localhost:3306` 的 MySQL。请按本地环境修改
[ShopKeeper_backend/src/main/resources/application.properties](ShopKeeper_backend/src/main/resources/application.properties)
中的数据库账号密码：

```properties
spring.datasource.username=你的用户名
spring.datasource.password=你的密码
```

然后启动：

```bash
cd ShopKeeper_backend
./mvnw spring-boot:run        # 或 mvn spring-boot:run
```

后端运行在 <http://localhost:8080>。

### 4. 启动前端

```bash
cd ShopKeeper_frontend
npm install
npm run dev
```

前端运行在 <http://localhost:5173>，已配置代理将 `/api` 转发到后端 8080 端口。

### 5. 登录

默认演示账号：**admin / 123456**

## 相关文档

- [ER 模型设计](docs/01-ER模型设计.md)
- [数据库设计](docs/02-数据库设计.md)
- [ER 图 Mermaid 代码](docs/03-ER图Mermaid代码.md)
