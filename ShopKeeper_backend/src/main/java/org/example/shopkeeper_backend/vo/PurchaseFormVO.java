package org.example.shopkeeper_backend.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 采购单详情/编辑回显（主表字段 + 明细行）
 */
@Data
public class PurchaseFormVO {

    private String poNo;
    private String supplierId;
    private LocalDate orderDate;
    private String deliveryPlace;
    private String status;
    private String remark;

    private List<PurchaseDetailVO> items;
}
