package org.example.shopkeeper_backend.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.shopkeeper_backend.entity.SaleOrder;
import org.example.shopkeeper_backend.vo.SaleDetailVO;
import org.example.shopkeeper_backend.vo.SaleOrderVO;

import java.util.List;

public interface SaleOrderMapper {

    long count(@Param("soNo") String soNo,
               @Param("customerId") String customerId,
               @Param("status") String status,
               @Param("startDate") String startDate,
               @Param("endDate") String endDate);

    List<SaleOrderVO> selectPage(@Param("soNo") String soNo,
                                 @Param("customerId") String customerId,
                                 @Param("status") String status,
                                 @Param("startDate") String startDate,
                                 @Param("endDate") String endDate,
                                 @Param("offset") int offset,
                                 @Param("size") int size);

    SaleOrder selectById(@Param("soNo") String soNo);

    /** 单据详情明细：查询完整信息视图 v_sale_full */
    List<SaleDetailVO> selectDetailBySoNo(@Param("soNo") String soNo);

    int insert(SaleOrder order);

    int update(SaleOrder order);

    /** 审核：更新状态 */
    int updateStatus(@Param("soNo") String soNo, @Param("status") String status);

    int deleteById(@Param("soNo") String soNo);

    /** 生成单号：查某日（yyyyMMdd）最大销售单号 */
    String selectMaxNoByDate(@Param("dateStr") String dateStr);
}
