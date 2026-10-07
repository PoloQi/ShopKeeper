-- =============================================================
-- 商品进销存管理系统 测试数据
-- 前置：先执行 init.sql
-- 用法：mysql -u root -p jxc_db < test-data.sql
-- 演示账号：admin / 123456
-- =============================================================

USE jxc_db;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE sale_item;
TRUNCATE TABLE sale_order;
TRUNCATE TABLE purchase_item;
TRUNCATE TABLE purchase_order;
TRUNCATE TABLE product;
TRUNCATE TABLE supplier;
TRUNCATE TABLE customer;
TRUNCATE TABLE sys_user;
SET FOREIGN_KEY_CHECKS = 1;

-- -------------------------------------------------------------
-- 用户（密码均为 123456 的 BCrypt 密文）
-- -------------------------------------------------------------
INSERT INTO sys_user (user_id, username, password, real_name, gender, status) VALUES
(1, 'admin',    '$2a$10$h/rhDQCy34lHe.ApfxhDNuHKW.AhBaI484YVe4VD5F8YwYpAj5O3y', '系统管理员', 'M', 1),
(2, 'zhangwei', '$2a$10$h/rhDQCy34lHe.ApfxhDNuHKW.AhBaI484YVe4VD5F8YwYpAj5O3y', '张伟',       'M', 1),
(3, 'lina',     '$2a$10$h/rhDQCy34lHe.ApfxhDNuHKW.AhBaI484YVe4VD5F8YwYpAj5O3y', '李娜',       'F', 1);

-- -------------------------------------------------------------
-- 供应商
-- -------------------------------------------------------------
INSERT INTO supplier (supplier_id, supplier_name, contact_person, phone, address, status) VALUES
('GYS001', '华南食品批发有限公司', '王建国', '020-88661234', '广州市白云区批发市场A栋', 1),
('GYS002', '顺达日用品贸易商行',   '陈丽',   '0755-26668899', '深圳市宝安区西乡街道12号', 1),
('GYS003', '广发电器批发商',       '刘明',   '0757-22334455', '佛山市顺德区容桂工业园',   1);

-- -------------------------------------------------------------
-- 客户
-- -------------------------------------------------------------
INSERT INTO customer (customer_id, customer_name, contact_person, phone, address, status) VALUES
('KH0001', '好日子超市',     '周敏', '020-33445566', '广州市天河区体育西路8号', 1),
('KH0002', '宜家便利店',     '吴强', '020-77889900', '广州市越秀区北京路200号', 1),
('KH0003', '惠民百货商场',   '郑芳', '0769-22113344', '东莞市南城区鸿福路66号', 1),
('KH0004', '社区团购自提点', '孙磊', '020-55667788', '广州市番禺区大学城',     0);

-- -------------------------------------------------------------
-- 商品
-- -------------------------------------------------------------
INSERT INTO product (product_id, product_name, category, spec, unit, unit_price, status) VALUES
('SP0001', '金龙鱼食用油', '食品',   '5L/桶',          '桶', 69.90, 1),
('SP0002', '东北大米',     '食品',   '10kg/袋',        '袋', 89.00, 1),
('SP0003', '可口可乐',     '食品',   '330ml*24罐',     '箱', 58.00, 1),
('SP0004', '乐事薯片',     '食品',   '70g',            '包', 6.50,  1),
('SP0005', '伊利纯牛奶',   '食品',   '250ml*24盒',     '箱', 65.00, 1),
('SP0006', '抽纸',         '日用品', '3层120抽*6包',   '提', 22.00, 1),
('SP0007', '洗衣液',       '日用品', '3kg',            '瓶', 35.00, 1),
('SP0008', '电热水壶',     '电器',   '1.8L',           '台', 79.00, 1),
('SP0009', '台灯',         '电器',   'LED护眼',        '台', 49.00, 1),
('SP0010', '笔记本',       '日用品', 'A5/100页',       '本', 5.50,  1);

-- -------------------------------------------------------------
-- 采购单
-- -------------------------------------------------------------
-- 1) 2026-09-15 向 GYS001 采购，已审核
INSERT INTO purchase_order (po_no, supplier_id, operator_id, order_date, delivery_place, status, remark) VALUES
('CG20260915001', 'GYS001', 1, '2026-09-15', '主仓1号库', '1', '中秋备货');
INSERT INTO purchase_item (po_no, product_id, quantity, unit_price, discount) VALUES
('CG20260915001', 'SP0001', 50, 58.00, 1.00),
('CG20260915001', 'SP0002', 40, 72.00, 1.00),
('CG20260915001', 'SP0005', 30, 52.00, 1.00);

-- 2) 2026-09-20 向 GYS002 采购，已审核
INSERT INTO purchase_order (po_no, supplier_id, operator_id, order_date, delivery_place, status, remark) VALUES
('CG20260920001', 'GYS002', 2, '2026-09-20', '主仓1号库', '1', NULL);
INSERT INTO purchase_item (po_no, product_id, quantity, unit_price, discount) VALUES
('CG20260920001', 'SP0006', 100, 16.00, 1.00),
('CG20260920001', 'SP0007', 60,  27.00, 1.00),
('CG20260920001', 'SP0010', 200, 3.50,  0.95);

-- 3) 2026-10-05 向 GYS003 采购，已审核
INSERT INTO purchase_order (po_no, supplier_id, operator_id, order_date, delivery_place, status, remark) VALUES
('CG20261005001', 'GYS003', 1, '2026-10-05', '主仓2号库', '1', '电器补货');
INSERT INTO purchase_item (po_no, product_id, quantity, unit_price, discount) VALUES
('CG20261005001', 'SP0008', 20, 60.00, 1.00),
('CG20261005001', 'SP0009', 25, 36.00, 1.00);

-- 4) 2026-10-06 向 GYS001 采购，【未审核】（不计入库存）
INSERT INTO purchase_order (po_no, supplier_id, operator_id, order_date, delivery_place, status, remark) VALUES
('CG20261006001', 'GYS001', 3, '2026-10-06', '主仓1号库', '0', '待检验');
INSERT INTO purchase_item (po_no, product_id, quantity, unit_price, discount) VALUES
('CG20261006001', 'SP0003', 40,  48.00, 1.00),
('CG20261006001', 'SP0004', 150, 4.50,  1.00);

-- -------------------------------------------------------------
-- 销售单
-- -------------------------------------------------------------
-- 1) 2026-09-18 售给 KH0001，已审核
INSERT INTO sale_order (so_no, customer_id, operator_id, order_date, delivery_place, status, remark) VALUES
('XS20260918001', 'KH0001', 1, '2026-09-18', '客户门店', '1', NULL);
INSERT INTO sale_item (so_no, product_id, quantity, unit_price, discount) VALUES
('XS20260918001', 'SP0001', 10, 69.90, 1.00),
('XS20260918001', 'SP0002', 8,  89.00, 1.00),
('XS20260918001', 'SP0005', 6,  65.00, 1.00);

-- 2) 2026-09-25 售给 KH0002，已审核
INSERT INTO sale_order (so_no, customer_id, operator_id, order_date, delivery_place, status, remark) VALUES
('XS20260925001', 'KH0002', 2, '2026-09-25', '客户门店', '1', '洗衣液促销');
INSERT INTO sale_item (so_no, product_id, quantity, unit_price, discount) VALUES
('XS20260925001', 'SP0006', 20, 22.00, 1.00),
('XS20260925001', 'SP0007', 5,  35.00, 0.95),
('XS20260925001', 'SP0010', 30, 5.50,  1.00);

-- 3) 2026-10-06 售给 KH0003，已审核
INSERT INTO sale_order (so_no, customer_id, operator_id, order_date, delivery_place, status, remark) VALUES
('XS20261006001', 'KH0003', 1, '2026-10-06', '客户商场', '1', NULL);
INSERT INTO sale_item (so_no, product_id, quantity, unit_price, discount) VALUES
('XS20261006001', 'SP0008', 3, 79.00, 1.00),
('XS20261006001', 'SP0009', 5, 49.00, 1.00),
('XS20261006001', 'SP0001', 5, 69.90, 1.00);

-- 4) 2026-10-07 售给 KH0002，【未审核】（不计入库存）
INSERT INTO sale_order (so_no, customer_id, operator_id, order_date, delivery_place, status, remark) VALUES
('XS20261007001', 'KH0002', 3, '2026-10-07', '客户门店', '0', NULL);
INSERT INTO sale_item (so_no, product_id, quantity, unit_price, discount) VALUES
('XS20261007001', 'SP0002', 4, 89.00, 1.00);
