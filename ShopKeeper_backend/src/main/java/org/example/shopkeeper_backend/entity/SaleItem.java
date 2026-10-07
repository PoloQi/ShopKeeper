package org.example.shopkeeper_backend.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售明细表
 */
@Data
public class SaleItem {

    /** 销售单号 */
    private String soNo;

    /** 商品编号 */
    private String productId;

    /** 数量 */
    private Integer quantity;

    /** 成交单价 */
    private BigDecimal unitPrice;

    /** 折扣 */
    private BigDecimal discount;
}
