package org.example.shopkeeper_backend.entity;

import lombok.Data;

/**
 * 客户
 */
@Data
public class Customer {

    /** 客户编号 KH0001 */
    private String customerId;

    /** 客户名称 */
    private String customerName;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String phone;

    /** 地址 */
    private String address;

    /** 1 启用 / 0 停用 */
    private Integer status;
}
