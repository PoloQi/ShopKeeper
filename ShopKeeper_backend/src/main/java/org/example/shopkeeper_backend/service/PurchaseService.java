package org.example.shopkeeper_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.BusinessException;
import org.example.shopkeeper_backend.common.PageResult;
import org.example.shopkeeper_backend.dto.PurchaseSaveDTO;
import org.example.shopkeeper_backend.entity.PurchaseItem;
import org.example.shopkeeper_backend.entity.PurchaseOrder;
import org.example.shopkeeper_backend.entity.SysUser;
import org.example.shopkeeper_backend.mapper.PurchaseItemMapper;
import org.example.shopkeeper_backend.mapper.PurchaseOrderMapper;
import org.example.shopkeeper_backend.vo.PurchaseFormVO;
import org.example.shopkeeper_backend.vo.PurchaseOrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseOrderMapper orderMapper;
    private final PurchaseItemMapper itemMapper;

    public PageResult<PurchaseOrderVO> page(String poNo, String supplierId, String status,
                                            String startDate, String endDate,
                                            int page, int size) {
        long total = orderMapper.count(poNo, supplierId, status, startDate, endDate);
        List<PurchaseOrderVO> records = total == 0
                ? List.of()
                : orderMapper.selectPage(poNo, supplierId, status, startDate, endDate,
                        (page - 1) * size, size);
        return new PageResult<>(total, records);
    }

    /** 详情：主表 + 视图明细 */
    public PurchaseFormVO getDetail(String poNo) {
        PurchaseOrder order = orderMapper.selectById(poNo);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }
        PurchaseFormVO vo = new PurchaseFormVO();
        BeanUtils.copyProperties(order, vo);
        vo.setItems(orderMapper.selectDetailByPoNo(poNo));
        return vo;
    }

    /**
     * 新增或修改采购单（主表 + 明细同一事务）
     * @return 采购单号
     */
    @Transactional
    public String save(PurchaseSaveDTO dto, SysUser loginUser) {
        validateItems(dto.getItems());

        if (dto.getPoNo() == null || dto.getPoNo().isBlank()) {
            // ---- 新增 ----
            String poNo = generateNo();

            PurchaseOrder order = new PurchaseOrder();
            order.setPoNo(poNo);
            order.setSupplierId(dto.getSupplierId());
            order.setOperatorId(loginUser.getUserId());
            order.setOrderDate(dto.getOrderDate());
            order.setDeliveryPlace(dto.getDeliveryPlace());
            order.setStatus("0");
            order.setRemark(dto.getRemark());
            orderMapper.insert(order);

            dto.getItems().forEach(it -> it.setPoNo(poNo));
            itemMapper.batchInsert(dto.getItems());
            return poNo;
        } else {
            // ---- 修改 ----
            PurchaseOrder old = orderMapper.selectById(dto.getPoNo());
            if (old == null) {
                throw new BusinessException("采购单不存在");
            }
            if ("1".equals(old.getStatus())) {
                throw new BusinessException("已审核单据不能修改");
            }

            old.setSupplierId(dto.getSupplierId());
            old.setOrderDate(dto.getOrderDate());
            old.setDeliveryPlace(dto.getDeliveryPlace());
            old.setRemark(dto.getRemark());
            orderMapper.update(old);

            // 明细先清空再重建
            itemMapper.deleteByPoNo(dto.getPoNo());
            dto.getItems().forEach(it -> it.setPoNo(dto.getPoNo()));
            itemMapper.batchInsert(dto.getItems());
            return dto.getPoNo();
        }
    }

    /** 删除（已审核禁删；明细由外键 CASCADE 一并删除） */
    @Transactional
    public void delete(String poNo) {
        PurchaseOrder order = orderMapper.selectById(poNo);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }
        if ("1".equals(order.getStatus())) {
            throw new BusinessException("已审核单据不能删除");
        }
        orderMapper.deleteById(poNo);
    }

    /** 审核 */
    public void audit(String poNo) {
        PurchaseOrder order = orderMapper.selectById(poNo);
        if (order == null) {
            throw new BusinessException("采购单不存在");
        }
        if ("1".equals(order.getStatus())) {
            throw new BusinessException("该单据已审核");
        }
        orderMapper.updateStatus(poNo, "1");
    }

    /** 生成单号：CG + 制单日期(yyyyMMdd) + 3 位当天流水 */
    private String generateNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String maxNo = orderMapper.selectMaxNoByDate(dateStr);
        int seq = maxNo == null ? 1 : Integer.parseInt(maxNo.substring(10)) + 1;
        return "CG" + dateStr + String.format("%03d", seq);
    }

    /** 明细合法性校验（数量、单价、折扣、商品重复） */
    private void validateItems(List<PurchaseItem> items) {
        Set<String> productIds = new HashSet<>();
        for (PurchaseItem it : items) {
            if (it.getProductId() == null || it.getProductId().isBlank()) {
                throw new BusinessException("请选择明细商品");
            }
            if (it.getQuantity() == null || it.getQuantity() <= 0) {
                throw new BusinessException("明细数量必须大于 0");
            }
            if (it.getUnitPrice() == null || it.getUnitPrice().signum() < 0) {
                throw new BusinessException("明细单价不能为负");
            }
            if (it.getDiscount() == null) {
                it.setDiscount(java.math.BigDecimal.ONE);
            }
            if (!productIds.add(it.getProductId())) {
                throw new BusinessException("同一商品在单据中重复，请合并数量");
            }
        }
    }
}
