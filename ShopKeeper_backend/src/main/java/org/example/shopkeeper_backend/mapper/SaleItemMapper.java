package org.example.shopkeeper_backend.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.shopkeeper_backend.entity.SaleItem;

import java.util.List;

public interface SaleItemMapper {

    /** 批量插入明细（保存单据时一次写入多行） */
    int batchInsert(@Param("list") List<SaleItem> list);

    /** 编辑单据：先按单号清空旧明细，再批量插入 */
    int deleteBySoNo(@Param("soNo") String soNo);
}
