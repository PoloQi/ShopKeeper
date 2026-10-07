package org.example.shopkeeper_backend.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 销售单列表行（主表 + 客户名称 + 合计金额）
 */
@Data
public class SaleOrderVO {

    private String soNo;
    private String customerId;
    private String customerName;
    private Long operatorId;
    private String operatorName;
    private LocalDate orderDate;
    private String deliveryPlace;
    private String status;
    private BigDecimal totalAmount;
    private String remark;
}
