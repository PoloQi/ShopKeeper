package org.example.shopkeeper_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.PageResult;
import org.example.shopkeeper_backend.entity.Customer;
import org.example.shopkeeper_backend.mapper.CustomerMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerMapper customerMapper;

    public PageResult<Customer> page(String customerName, String phone, Integer status,
                                     int page, int size) {
        long total = customerMapper.count(customerName, phone, status);
        List<Customer> records = total == 0
                ? List.of()
                : customerMapper.selectPage(customerName, phone, status, (page - 1) * size, size);
        return new PageResult<>(total, records);
    }

    public Customer getById(String customerId) {
        return customerMapper.selectById(customerId);
    }

    /** 下拉选项 */
    public List<Customer> options() {
        return customerMapper.selectAllActive();
    }

    /** 新增：自动生成 KH+4位流水 编号，返回编号 */
    public String add(Customer customer) {
        String maxId = customerMapper.selectMaxId();
        int seq = maxId == null ? 1 : Integer.parseInt(maxId.substring(2)) + 1;
        customer.setCustomerId(String.format("KH%04d", seq));
        customerMapper.insert(customer);
        return customer.getCustomerId();
    }

    public void update(Customer customer) {
        customerMapper.update(customer);
    }

    public void delete(String customerId) {
        customerMapper.deleteById(customerId);
    }
}
