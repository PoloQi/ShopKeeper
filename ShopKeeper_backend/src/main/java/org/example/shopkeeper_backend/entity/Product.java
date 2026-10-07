package org.example.shopkeeper_backend.entity;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品
 */
@Data
public class Product {

    /** 商品编号 SP0001 */
    private String productId;

    /** 商品名称 */
    private String productName;

    /** 分类 */
    private String category;

    /** 规格型号 */
    private String spec;

    /** 计量单位 */
    private String unit;

    /** 标准单价 */
    private BigDecimal unitPrice;

    /** 1 在售 / 0 停售 */
    private Integer status;
}
