package org.example.shopkeeper_backend.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.shopkeeper_backend.entity.Supplier;

import java.util.List;

public interface SupplierMapper {

    long count(@Param("supplierName") String supplierName,
               @Param("phone") String phone,
               @Param("status") Integer status);

    List<Supplier> selectPage(@Param("supplierName") String supplierName,
                              @Param("phone") String phone,
                              @Param("status") Integer status,
                              @Param("offset") int offset,
                              @Param("size") int size);

    Supplier selectById(@Param("supplierId") String supplierId);

    /** 下拉选项：仅启用供应商 */
    List<Supplier> selectAllActive();

    /** 生成编号：查当前最大供应商编号 */
    String selectMaxId();

    int insert(Supplier supplier);

    int update(Supplier supplier);

    int deleteById(@Param("supplierId") String supplierId);
}
