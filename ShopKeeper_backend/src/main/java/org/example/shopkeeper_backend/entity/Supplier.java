package org.example.shopkeeper_backend.entity;

import lombok.Data;

/**
 * 供应商
 */
@Data
public class Supplier {

    /** 供应商编号 GYS001 */
    private String supplierId;

    /** 供应商名称 */
    private String supplierName;

    /** 联系人 */
    private String contactPerson;

    /** 联系电话 */
    private String phone;

    /** 地址 */
    private String address;

    /** 1 启用 / 0 停用 */
    private Integer status;
}
