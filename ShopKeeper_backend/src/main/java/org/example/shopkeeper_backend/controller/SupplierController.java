package org.example.shopkeeper_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.OwnerOnly;
import org.example.shopkeeper_backend.common.PageResult;
import org.example.shopkeeper_backend.common.Result;
import org.example.shopkeeper_backend.entity.Supplier;
import org.example.shopkeeper_backend.service.SupplierService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    /** 分页 + 多条件组合查询 */
    @GetMapping
    public Result<PageResult<Supplier>> page(
            @RequestParam(required = false) String supplierName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(supplierService.page(supplierName, phone, status, page, size));
    }

    /** 下拉选项 */
    @GetMapping("/options")
    public Result<List<Supplier>> options() {
        return Result.ok(supplierService.options());
    }

    @GetMapping("/{supplierId}")
    public Result<Supplier> getById(@PathVariable String supplierId) {
        return Result.ok(supplierService.getById(supplierId));
    }

    /** 新增（编号后端生成并返回） */
    @PostMapping
    public Result<String> add(@RequestBody Supplier supplier) {
        return Result.ok(supplierService.add(supplier));
    }

    @PutMapping
    public Result<Void> update(@RequestBody Supplier supplier) {
        supplierService.update(supplier);
        return Result.ok();
    }

    @OwnerOnly
    @DeleteMapping("/{supplierId}")
    public Result<Void> delete(@PathVariable String supplierId) {
        supplierService.delete(supplierId);
        return Result.ok();
    }
}
