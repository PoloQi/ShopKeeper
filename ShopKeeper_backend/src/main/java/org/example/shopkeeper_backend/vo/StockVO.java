package org.example.shopkeeper_backend.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 库存视图 v_stock 行
 */
@Data
public class StockVO {

    private String productId;
    private String productName;
    private String spec;
    private String unit;
    private BigDecimal unitPrice;

    /** 累计已审核入库数量 */
    private Integer purchaseQty;

    /** 累计已审核出库数量 */
    private Integer saleQty;

    /** 实时库存 = 入库 - 出库 */
    private Integer stockQty;
}
