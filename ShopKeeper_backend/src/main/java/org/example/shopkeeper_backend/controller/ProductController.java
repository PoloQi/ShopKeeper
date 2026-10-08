package org.example.shopkeeper_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.OwnerOnly;
import org.example.shopkeeper_backend.common.PageResult;
import org.example.shopkeeper_backend.common.Result;
import org.example.shopkeeper_backend.entity.Product;
import org.example.shopkeeper_backend.service.ProductService;
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
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** 分页 + 多条件组合查询 */
    @GetMapping
    public Result<PageResult<Product>> page(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(productService.page(productName, category, status, page, size));
    }

    /** 下拉选项 */
    @GetMapping("/options")
    public Result<List<Product>> options() {
        return Result.ok(productService.options());
    }

    @GetMapping("/{productId}")
    public Result<Product> getById(@PathVariable String productId) {
        return Result.ok(productService.getById(productId));
    }

    /** 新增（编号后端生成并返回） */
    @PostMapping
    public Result<String> add(@RequestBody Product product) {
        return Result.ok(productService.add(product));
    }

    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        productService.update(product);
        return Result.ok();
    }

    @OwnerOnly
    @DeleteMapping("/{productId}")
    public Result<Void> delete(@PathVariable String productId) {
        productService.delete(productId);
        return Result.ok();
    }
}
