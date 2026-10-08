-- =============================================================
-- 商品进销存管理系统 数据库初始化脚本
-- MySQL 8.0 / InnoDB / utf8mb4
-- 用法：mysql -u root -p < init.sql
-- =============================================================

DROP DATABASE IF EXISTS jxc_db;
CREATE DATABASE jxc_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE jxc_db;

-- 重跑时先清理视图（视图依赖表）
DROP VIEW IF EXISTS v_stock;
DROP VIEW IF EXISTS v_sale_full;
DROP VIEW IF EXISTS v_purchase_full;

-- -------------------------------------------------------------
-- 1. 用户表
-- -------------------------------------------------------------
CREATE TABLE sys_user (
    user_id    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '用户编号',
    username   VARCHAR(50)  NOT NULL COMMENT '登录用户名',
    password   VARCHAR(100) NOT NULL COMMENT 'BCrypt密码密文',
    real_name  VARCHAR(50)  NOT NULL COMMENT '真实姓名',
    gender     CHAR(1)      NOT NULL COMMENT '性别 M男 F女',
    role       TINYINT      NOT NULL DEFAULT 0 COMMENT '1店长 0店员',
    status     TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_username (username),
    CONSTRAINT ck_user_gender CHECK (gender IN ('M','F')),
    CONSTRAINT ck_user_role CHECK (role IN (0,1)),
    CONSTRAINT ck_user_status CHECK (status IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- -------------------------------------------------------------
-- 2. 客户表
-- -------------------------------------------------------------
CREATE TABLE customer (
    customer_id    CHAR(6)      NOT NULL COMMENT '客户编号 KH0001',
    customer_name  VARCHAR(100) NOT NULL COMMENT '客户名称',
    contact_person VARCHAR(50)  DEFAULT NULL COMMENT '联系人',
    phone          VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    address        VARCHAR(200) DEFAULT NULL COMMENT '地址',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    PRIMARY KEY (customer_id),
    KEY idx_customer_name (customer_name),
    KEY idx_phone (phone),
    CONSTRAINT ck_customer_status CHECK (status IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

-- -------------------------------------------------------------
-- 3. 供应商表
-- -------------------------------------------------------------
CREATE TABLE supplier (
    supplier_id    CHAR(6)      NOT NULL COMMENT '供应商编号 GYS001',
    supplier_name  VARCHAR(100) NOT NULL COMMENT '供应商名称',
    contact_person VARCHAR(50)  DEFAULT NULL COMMENT '联系人',
    phone          VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
    address        VARCHAR(200) DEFAULT NULL COMMENT '地址',
    status         TINYINT      NOT NULL DEFAULT 1 COMMENT '1启用 0停用',
    PRIMARY KEY (supplier_id),
    KEY idx_supplier_name (supplier_name),
    KEY idx_phone (phone),
    CONSTRAINT ck_supplier_status CHECK (status IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='供应商表';

-- -------------------------------------------------------------
-- 4. 商品表
-- -------------------------------------------------------------
CREATE TABLE product (
    product_id   CHAR(6)       NOT NULL COMMENT '商品编号 SP0001',
    product_name VARCHAR(100)  NOT NULL COMMENT '商品名称',
    category     VARCHAR(30)   DEFAULT NULL COMMENT '分类',
    spec         VARCHAR(50)   DEFAULT NULL COMMENT '规格型号',
    unit         VARCHAR(10)   NOT NULL COMMENT '计量单位',
    unit_price   DECIMAL(10,2) NOT NULL COMMENT '标准单价',
    status       TINYINT       NOT NULL DEFAULT 1 COMMENT '1在售 0停售',
    PRIMARY KEY (product_id),
    KEY idx_product_name (product_name),
    KEY idx_category (category),
    CONSTRAINT ck_product_price  CHECK (unit_price >= 0),
    CONSTRAINT ck_product_status CHECK (status IN (0,1))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- -------------------------------------------------------------
-- 5. 采购单主表
-- -------------------------------------------------------------
CREATE TABLE purchase_order (
    po_no          CHAR(13)      NOT NULL COMMENT '采购单号 CG+日期+流水',
    supplier_id    CHAR(6)       NOT NULL COMMENT '供应商编号',
    operator_id    BIGINT        NOT NULL COMMENT '经手人用户编号',
    order_date     DATE          NOT NULL COMMENT '单据日期',
    delivery_place VARCHAR(100)  DEFAULT NULL COMMENT '交货地点',
    status         CHAR(1)       NOT NULL DEFAULT '0' COMMENT '0未审核 1已审核',
    remark         VARCHAR(200)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (po_no),
    KEY idx_supplier_id (supplier_id),
    KEY idx_order_date (order_date),
    KEY idx_status (status),
    KEY idx_operator_id (operator_id),
    CONSTRAINT fk_po_supplier FOREIGN KEY (supplier_id) REFERENCES supplier (supplier_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_po_user FOREIGN KEY (operator_id) REFERENCES sys_user (user_id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_po_status CHECK (status IN ('0','1'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购单主表';

-- -------------------------------------------------------------
-- 6. 采购明细表
-- -------------------------------------------------------------
CREATE TABLE purchase_item (
    po_no       CHAR(13)      NOT NULL COMMENT '采购单号',
    product_id  CHAR(6)       NOT NULL COMMENT '商品编号',
    quantity    INT           NOT NULL COMMENT '数量',
    unit_price  DECIMAL(10,2) NOT NULL COMMENT '成交单价',
    discount    DECIMAL(3,2)  NOT NULL DEFAULT 1.00 COMMENT '折扣',
    PRIMARY KEY (po_no, product_id),
    KEY idx_product_id (product_id),
    CONSTRAINT fk_pi_po FOREIGN KEY (po_no) REFERENCES purchase_order (po_no)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pi_product FOREIGN KEY (product_id) REFERENCES product (product_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_pi_quantity CHECK (quantity > 0),
    CONSTRAINT ck_pi_price    CHECK (unit_price >= 0),
    CONSTRAINT ck_pi_discount CHECK (discount BETWEEN 0 AND 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='采购明细表';

-- -------------------------------------------------------------
-- 7. 销售单主表
-- -------------------------------------------------------------
CREATE TABLE sale_order (
    so_no          CHAR(13)      NOT NULL COMMENT '销售单号 XS+日期+流水',
    customer_id    CHAR(6)       NOT NULL COMMENT '客户编号',
    operator_id    BIGINT        NOT NULL COMMENT '经手人用户编号',
    order_date     DATE          NOT NULL COMMENT '单据日期',
    delivery_place VARCHAR(100)  DEFAULT NULL COMMENT '交货地点',
    status         CHAR(1)       NOT NULL DEFAULT '0' COMMENT '0未审核 1已审核',
    remark         VARCHAR(200)  DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (so_no),
    KEY idx_customer_id (customer_id),
    KEY idx_order_date (order_date),
    KEY idx_status (status),
    KEY idx_operator_id (operator_id),
    CONSTRAINT fk_so_customer FOREIGN KEY (customer_id) REFERENCES customer (customer_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_so_user FOREIGN KEY (operator_id) REFERENCES sys_user (user_id)
        ON DELETE RESTRICT,
    CONSTRAINT ck_so_status CHECK (status IN ('0','1'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售单主表';

-- -------------------------------------------------------------
-- 8. 销售明细表
-- -------------------------------------------------------------
CREATE TABLE sale_item (
    so_no       CHAR(13)      NOT NULL COMMENT '销售单号',
    product_id  CHAR(6)       NOT NULL COMMENT '商品编号',
    quantity    INT           NOT NULL COMMENT '数量',
    unit_price  DECIMAL(10,2) NOT NULL COMMENT '成交单价',
    discount    DECIMAL(3,2)  NOT NULL DEFAULT 1.00 COMMENT '折扣',
    PRIMARY KEY (so_no, product_id),
    KEY idx_product_id (product_id),
    CONSTRAINT fk_si_so FOREIGN KEY (so_no) REFERENCES sale_order (so_no)
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_si_product FOREIGN KEY (product_id) REFERENCES product (product_id)
        ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT ck_si_quantity CHECK (quantity > 0),
    CONSTRAINT ck_si_price    CHECK (unit_price >= 0),
    CONSTRAINT ck_si_discount CHECK (discount BETWEEN 0 AND 1)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售明细表';

-- -------------------------------------------------------------
-- 视图1：采购单完整信息
-- -------------------------------------------------------------
CREATE VIEW v_purchase_full AS
SELECT
       pi.po_no,
       po.order_date,
       po.delivery_place,
       po.status,
       s.supplier_id,
       s.supplier_name,
       pi.product_id,
       p.product_name,
       p.spec,
       p.unit,
       pi.quantity,
       pi.unit_price,
       pi.discount,
       ROUND(pi.quantity * pi.unit_price * pi.discount, 2) AS amount,
       u.user_id,
       u.real_name AS operator_name,
       po.remark
FROM purchase_item pi
JOIN purchase_order po ON pi.po_no = po.po_no
JOIN supplier       s  ON po.supplier_id = s.supplier_id
JOIN product        p  ON pi.product_id = p.product_id
JOIN sys_user       u  ON po.operator_id = u.user_id;

-- -------------------------------------------------------------
-- 视图2：销售单完整信息
-- -------------------------------------------------------------
CREATE VIEW v_sale_full AS
SELECT
       si.so_no,
       so.order_date,
       so.delivery_place,
       so.status,
       c.customer_id,
       c.customer_name,
       si.product_id,
       p.product_name,
       p.spec,
       p.unit,
       si.quantity,
       si.unit_price,
       si.discount,
       ROUND(si.quantity * si.unit_price * si.discount, 2) AS amount,
       u.user_id,
       u.real_name AS operator_name,
       so.remark
FROM sale_item si
JOIN sale_order  so ON si.so_no = so.so_no
JOIN customer    c  ON so.customer_id = c.customer_id
JOIN product     p  ON si.product_id = p.product_id
JOIN sys_user    u  ON so.operator_id = u.user_id;

-- -------------------------------------------------------------
-- 视图3：商品实时库存（已审核采购 - 已审核销售）
-- -------------------------------------------------------------
CREATE VIEW v_stock AS
SELECT
       p.product_id,
       p.product_name,
       p.spec,
       p.unit,
       p.unit_price,
       COALESCE(pur.qty, 0) AS purchase_qty,
       COALESCE(sal.qty, 0) AS sale_qty,
       COALESCE(pur.qty, 0) - COALESCE(sal.qty, 0) AS stock_qty
FROM product p
LEFT JOIN (
        SELECT pi.product_id, SUM(pi.quantity) AS qty
        FROM purchase_item pi
        JOIN purchase_order po ON pi.po_no = po.po_no
        WHERE po.status = '1'
        GROUP BY pi.product_id
) pur ON p.product_id = pur.product_id
LEFT JOIN (
        SELECT si.product_id, SUM(si.quantity) AS qty
        FROM sale_item si
        JOIN sale_order so ON si.so_no = so.so_no
        WHERE so.status = '1'
        GROUP BY si.product_id
) sal ON p.product_id = sal.product_id;
