package org.example.shopkeeper_backend.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.shopkeeper_backend.entity.Customer;

import java.util.List;

public interface CustomerMapper {

    long count(@Param("customerName") String customerName,
               @Param("phone") String phone,
               @Param("status") Integer status);

    List<Customer> selectPage(@Param("customerName") String customerName,
                              @Param("phone") String phone,
                              @Param("status") Integer status,
                              @Param("offset") int offset,
                              @Param("size") int size);

    Customer selectById(@Param("customerId") String customerId);

    /** 下拉选项：仅启用客户 */
    List<Customer> selectAllActive();

    /** 生成编号：查当前最大客户编号 */
    String selectMaxId();

    int insert(Customer customer);

    int update(Customer customer);

    int deleteById(@Param("customerId") String customerId);
}
