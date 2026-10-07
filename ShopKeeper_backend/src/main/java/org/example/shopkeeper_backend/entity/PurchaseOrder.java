package org.example.shopkeeper_backend.entity;

import lombok.Data;

import java.time.LocalDate;

/**
 * 采购单主表
 */
@Data
public class PurchaseOrder {

    /** 采购单号 CG+日期+流水 */
    private String poNo;

    /** 供应商编号 */
    private String supplierId;

    /** 经手人用户编号 */
    private Long operatorId;

    /** 单据日期 */
    private LocalDate orderDate;

    /** 交货地点 */
    private String deliveryPlace;

    /** 0 未审核 / 1 已审核 */
    private String status;

    /** 备注 */
    private String remark;
}
