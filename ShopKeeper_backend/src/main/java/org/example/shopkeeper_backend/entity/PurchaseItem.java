package org.example.shopkeeper_backend.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购明细表
 */
@Data
public class PurchaseItem {

    /** 采购单号 */
    private String poNo;

    /** 商品编号 */
    private String productId;

    /** 数量 */
    private Integer quantity;

    /** 成交单价 */
    private BigDecimal unitPrice;

    /** 折扣 */
    private BigDecimal discount;
}
