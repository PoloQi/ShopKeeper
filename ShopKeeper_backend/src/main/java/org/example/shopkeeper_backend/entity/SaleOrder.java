package org.example.shopkeeper_backend.entity;

import lombok.Data;

import java.time.LocalDate;

/**
 * 销售单主表
 */
@Data
public class SaleOrder {

    /** 销售单号 XS+日期+流水 */
    private String soNo;

    /** 客户编号 */
    private String customerId;

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
