package org.example.shopkeeper_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.OwnerOnly;
import org.example.shopkeeper_backend.common.PageResult;
import org.example.shopkeeper_backend.common.Result;
import org.example.shopkeeper_backend.entity.Customer;
import org.example.shopkeeper_backend.service.CustomerService;
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
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    /** 分页 + 多条件组合查询 */
    @GetMapping
    public Result<PageResult<Customer>> page(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(customerService.page(customerName, phone, status, page, size));
    }

    /** 下拉选项 */
    @GetMapping("/options")
    public Result<List<Customer>> options() {
        return Result.ok(customerService.options());
    }

    @GetMapping("/{customerId}")
    public Result<Customer> getById(@PathVariable String customerId) {
        return Result.ok(customerService.getById(customerId));
    }

    /** 新增（编号后端生成并返回） */
    @PostMapping
    public Result<String> add(@RequestBody Customer customer) {
        return Result.ok(customerService.add(customer));
    }

    @PutMapping
    public Result<Void> update(@RequestBody Customer customer) {
        customerService.update(customer);
        return Result.ok();
    }

    @OwnerOnly
    @DeleteMapping("/{customerId}")
    public Result<Void> delete(@PathVariable String customerId) {
        customerService.delete(customerId);
        return Result.ok();
    }
}
