package org.example.shopkeeper_backend.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.OwnerOnly;
import org.example.shopkeeper_backend.common.PageResult;
import org.example.shopkeeper_backend.common.Result;
import org.example.shopkeeper_backend.dto.SaleSaveDTO;
import org.example.shopkeeper_backend.entity.SysUser;
import org.example.shopkeeper_backend.interceptor.LoginInterceptor;
import org.example.shopkeeper_backend.service.SaleService;
import org.example.shopkeeper_backend.vo.SaleFormVO;
import org.example.shopkeeper_backend.vo.SaleOrderVO;
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
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    /** 分页 + 多条件组合查询（单号/客户/状态/日期范围） */
    @GetMapping
    public Result<PageResult<SaleOrderVO>> page(
            @RequestParam(required = false) String soNo,
            @RequestParam(required = false) String customerId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(saleService.page(soNo, customerId, status, startDate, endDate, page, size));
    }

    /** 详情（主表 + 明细） */
    @GetMapping("/{soNo}")
    public Result<SaleFormVO> getDetail(@PathVariable String soNo) {
        return Result.ok(saleService.getDetail(soNo));
    }

    /** 新增或修改（主从同事务；编号后端生成） */
    @PostMapping
    public Result<String> save(@Valid @RequestBody SaleSaveDTO dto, HttpSession session) {
        SysUser loginUser = (SysUser) session.getAttribute(LoginInterceptor.SESSION_USER);
        return Result.ok(saleService.save(dto, loginUser));
    }

    /** 删除（已审核禁删） */
    @OwnerOnly
    @DeleteMapping("/{soNo}")
    public Result<Void> delete(@PathVariable String soNo) {
        saleService.delete(soNo);
        return Result.ok();
    }

    /** 审核 */
    @OwnerOnly
    @PutMapping("/{soNo}/audit")
    public Result<Void> audit(@PathVariable String soNo) {
        saleService.audit(soNo);
        return Result.ok();
    }
}
