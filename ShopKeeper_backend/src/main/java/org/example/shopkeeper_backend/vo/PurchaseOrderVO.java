package org.example.shopkeeper_backend.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 采购单列表行（主表 + 供应商名称 + 合计金额）
 */
@Data
public class PurchaseOrderVO {

    private String poNo;
    private String supplierId;
    private String supplierName;
    private Long operatorId;
    private String operatorName;
    private LocalDate orderDate;
    private String deliveryPlace;
    private String status;
    private BigDecimal totalAmount;
    private String remark;
}
