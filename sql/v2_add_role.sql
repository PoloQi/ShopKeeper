-- -------------------------------------------------------------
-- v2 升级：sys_user 增加角色字段（店长 1 / 店员 0）
-- 适用于已按 init.sql 初始化过的存量数据库
-- -------------------------------------------------------------
ALTER TABLE sys_user
    ADD COLUMN role TINYINT NOT NULL DEFAULT 0 COMMENT '1店长 0店员' AFTER gender,
    ADD CONSTRAINT ck_user_role CHECK (role IN (0,1));

-- 存量账号：admin 设为店长，其余账号默认店员
UPDATE sys_user SET role = 1 WHERE username = 'admin';
