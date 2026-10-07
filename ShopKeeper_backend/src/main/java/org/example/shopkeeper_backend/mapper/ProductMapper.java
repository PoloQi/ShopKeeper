package org.example.shopkeeper_backend.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.shopkeeper_backend.entity.Product;

import java.util.List;

public interface ProductMapper {

    long count(@Param("productName") String productName,
               @Param("category") String category,
               @Param("status") Integer status);

    List<Product> selectPage(@Param("productName") String productName,
                             @Param("category") String category,
                             @Param("status") Integer status,
                             @Param("offset") int offset,
                             @Param("size") int size);

    Product selectById(@Param("productId") String productId);

    /** 下拉选项：仅在售商品 */
    List<Product> selectAllActive();

    int insert(Product product);

    int update(Product product);

    int deleteById(@Param("productId") String productId);

    /** 新增编号：查当前最大商品编号 */
    String selectMaxId();
}
