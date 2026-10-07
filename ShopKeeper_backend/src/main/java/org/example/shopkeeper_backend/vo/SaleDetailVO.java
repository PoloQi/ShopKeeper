package org.example.shopkeeper_backend.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 销售单详情中的明细行（来自 v_sale_full）
 */
@Data
public class SaleDetailVO {

    private String productId;
    private String productName;
    private String spec;
    private String unit;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal discount;

    /** 行金额（视图导出：单价×数量×折扣） */
    private BigDecimal amount;
}
