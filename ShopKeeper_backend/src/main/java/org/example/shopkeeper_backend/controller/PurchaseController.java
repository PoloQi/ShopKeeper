package org.example.shopkeeper_backend.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.PageResult;
import org.example.shopkeeper_backend.common.Result;
import org.example.shopkeeper_backend.dto.PurchaseSaveDTO;
import org.example.shopkeeper_backend.entity.SysUser;
import org.example.shopkeeper_backend.interceptor.LoginInterceptor;
import org.example.shopkeeper_backend.service.PurchaseService;
import org.example.shopkeeper_backend.vo.PurchaseFormVO;
import org.example.shopkeeper_backend.vo.PurchaseOrderVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    /** 分页 + 多条件组合查询（单号/供应商/状态/日期范围） */
    @GetMapping
    public Result<PageResult<PurchaseOrderVO>> page(
            @RequestParam(required = false) String poNo,
            @RequestParam(required = false) String supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(purchaseService.page(poNo, supplierId, status, startDate, endDate, page, size));
    }

    /** 详情（主表 + 明细） */
    @GetMapping("/{poNo}")
    public Result<PurchaseFormVO> getDetail(@PathVariable String poNo) {
        return Result.ok(purchaseService.getDetail(poNo));
    }

    /** 新增或修改（主从同事务；编号后端生成） */
    @PostMapping
    public Result<String> save(@Valid @RequestBody PurchaseSaveDTO dto, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute(LoginInterceptor.SESSION_USER);
        return Result.ok(purchaseService.save(dto, loginUser));
    }

    /** 删除（已审核禁删） */
    @DeleteMapping("/{poNo}")
    public Result<Void> delete(@PathVariable String poNo) {
        purchaseService.delete(poNo);
        return Result.ok();
    }

    /** 审核 */
    @PutMapping("/{poNo}/audit")
    public Result<Void> audit(@PathVariable String poNo) {
        purchaseService.audit(poNo);
        return Result.ok();
    }
}
