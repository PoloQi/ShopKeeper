package org.example.shopkeeper_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.example.shopkeeper_backend.entity.PurchaseItem;

import java.time.LocalDate;
import java.util.List;

/**
 * 采购单保存请求（主表 + 明细）
 */
@Data
public class PurchaseSaveDTO {

    /** 修改时有值；新增时为空，由后端生成 */
    private String poNo;

    @NotBlank(message = "请选择供应商")
    private String supplierId;

    @NotNull(message = "请选择单据日期")
    private LocalDate orderDate;

    private String deliveryPlace;

    private String remark;

    @NotEmpty(message = "请至少添加一条采购明细")
    @Valid
    private List<PurchaseItem> items;
}
