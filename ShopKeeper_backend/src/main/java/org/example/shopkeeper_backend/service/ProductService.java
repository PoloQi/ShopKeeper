package org.example.shopkeeper_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.PageResult;
import org.example.shopkeeper_backend.entity.Product;
import org.example.shopkeeper_backend.mapper.ProductMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;

    public PageResult<Product> page(String productName, String category, Integer status,
                                    int page, int size) {
        long total = productMapper.count(productName, category, status);
        List<Product> records = total == 0
                ? List.of()
                : productMapper.selectPage(productName, category, status, (page - 1) * size, size);
        return new PageResult<>(total, records);
    }

    public Product getById(String productId) {
        return productMapper.selectById(productId);
    }

    /** 下拉选项 */
    public List<Product> options() {
        return productMapper.selectAllActive();
    }

    /** 新增：自动生成 SP+4位流水 编号，返回编号 */
    public String add(Product product) {
        String maxId = productMapper.selectMaxId();
        int seq = maxId == null ? 1 : Integer.parseInt(maxId.substring(2)) + 1;
        product.setProductId(String.format("SP%04d", seq));
        productMapper.insert(product);
        return product.getProductId();
    }

    public void update(Product product) {
        productMapper.update(product);
    }

    public void delete(String productId) {
        productMapper.deleteById(productId);
    }
}
