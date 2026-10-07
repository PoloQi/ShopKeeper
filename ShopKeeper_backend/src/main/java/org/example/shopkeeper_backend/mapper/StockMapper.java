package org.example.shopkeeper_backend.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.shopkeeper_backend.vo.StockVO;

import java.util.List;

/**
 * 库存查询：数据来自实时库存视图 v_stock
 */
public interface StockMapper {

    List<StockVO> selectStockList(@Param("productName") String productName,
                                  @Param("productId") String productId,
                                  @Param("onlyPositive") boolean onlyPositive);
}
