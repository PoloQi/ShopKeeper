package org.example.shopkeeper_backend.mapper;

import org.apache.ibatis.annotations.Param;
import org.example.shopkeeper_backend.entity.PurchaseOrder;
import org.example.shopkeeper_backend.vo.PurchaseDetailVO;
import org.example.shopkeeper_backend.vo.PurchaseOrderVO;

import java.util.List;

public interface PurchaseOrderMapper {

    long count(@Param("poNo") String poNo,
               @Param("supplierId") String supplierId,
               @Param("status") String status,
               @Param("startDate") String startDate,
               @Param("endDate") String endDate);

    List<PurchaseOrderVO> selectPage(@Param("poNo") String poNo,
                                     @Param("supplierId") String supplierId,
                                     @Param("status") String status,
                                     @Param("startDate") String startDate,
                                     @Param("endDate") String endDate,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    PurchaseOrder selectById(@Param("poNo") String poNo);

    /** 单据详情明细：查询完整信息视图 v_purchase_full */
    List<PurchaseDetailVO> selectDetailByPoNo(@Param("poNo") String poNo);

    int insert(PurchaseOrder order);

    int update(PurchaseOrder order);

    /** 审核：更新状态 */
    int updateStatus(@Param("poNo") String poNo, @Param("status") String status);

    int deleteById(@Param("poNo") String poNo);

    /** 生成单号：查某日（yyyyMMdd）最大采购单号 */
    String selectMaxNoByDate(@Param("dateStr") String dateStr);
}
