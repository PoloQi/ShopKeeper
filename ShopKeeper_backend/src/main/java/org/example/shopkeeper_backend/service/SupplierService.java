package org.example.shopkeeper_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.PageResult;
import org.example.shopkeeper_backend.common.PhoneValidator;
import org.example.shopkeeper_backend.entity.Supplier;
import org.example.shopkeeper_backend.mapper.SupplierMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplierService {

    private final SupplierMapper supplierMapper;

    public PageResult<Supplier> page(String supplierName, String phone, Integer status,
                                     int page, int size) {
        long total = supplierMapper.count(supplierName, phone, status);
        List<Supplier> records = total == 0
                ? List.of()
                : supplierMapper.selectPage(supplierName, phone, status, (page - 1) * size, size);
        return new PageResult<>(total, records);
    }

    public Supplier getById(String supplierId) {
        return supplierMapper.selectById(supplierId);
    }

    /** 下拉选项 */
    public List<Supplier> options() {
        return supplierMapper.selectAllActive();
    }

    /** 新增：自动生成 GYS+3位流水 编号，返回编号 */
    public String add(Supplier supplier) {
        supplier.setPhone(PhoneValidator.normalize(supplier.getPhone()));
        PhoneValidator.check(supplier.getPhone());
        String maxId = supplierMapper.selectMaxId();
        int seq = maxId == null ? 1 : Integer.parseInt(maxId.substring(3)) + 1;
        supplier.setSupplierId(String.format("GYS%03d", seq));
        supplierMapper.insert(supplier);
        return supplier.getSupplierId();
    }

    public void update(Supplier supplier) {
        supplier.setPhone(PhoneValidator.normalize(supplier.getPhone()));
        PhoneValidator.check(supplier.getPhone());
        supplierMapper.update(supplier);
    }

    public void delete(String supplierId) {
        supplierMapper.deleteById(supplierId);
    }
}
