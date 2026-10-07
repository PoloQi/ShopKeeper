package org.example.shopkeeper_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.BusinessException;
import org.example.shopkeeper_backend.common.PageResult;
import org.example.shopkeeper_backend.dto.SaleSaveDTO;
import org.example.shopkeeper_backend.entity.SaleItem;
import org.example.shopkeeper_backend.entity.SaleOrder;
import org.example.shopkeeper_backend.entity.SysUser;
import org.example.shopkeeper_backend.mapper.SaleItemMapper;
import org.example.shopkeeper_backend.mapper.SaleOrderMapper;
import org.example.shopkeeper_backend.vo.SaleFormVO;
import org.example.shopkeeper_backend.vo.SaleOrderVO;
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
public class SaleService {

    private final SaleOrderMapper orderMapper;
    private final SaleItemMapper itemMapper;

    public PageResult<SaleOrderVO> page(String soNo, String customerId, String status,
                                        String startDate, String endDate,
                                        int page, int size) {
        long total = orderMapper.count(soNo, customerId, status, startDate, endDate);
        List<SaleOrderVO> records = total == 0
                ? List.of()
                : orderMapper.selectPage(soNo, customerId, status, startDate, endDate,
                        (page - 1) * size, size);
        return new PageResult<>(total, records);
    }

    /** 详情：主表 + 视图明细 */
    public SaleFormVO getDetail(String soNo) {
        SaleOrder order = orderMapper.selectById(soNo);
        if (order == null) {
            throw new BusinessException("销售单不存在");
        }
        SaleFormVO vo = new SaleFormVO();
        BeanUtils.copyProperties(order, vo);
        vo.setItems(orderMapper.selectDetailBySoNo(soNo));
        return vo;
    }

    /**
     * 新增或修改销售单（主表 + 明细同一事务）
     * @return 销售单号
     */
    @Transactional
    public String save(SaleSaveDTO dto, SysUser loginUser) {
        validateItems(dto.getItems());

        if (dto.getSoNo() == null || dto.getSoNo().isBlank()) {
            // ---- 新增 ----
            String soNo = generateNo();

            SaleOrder order = new SaleOrder();
            order.setSoNo(soNo);
            order.setCustomerId(dto.getCustomerId());
            order.setOperatorId(loginUser.getUserId());
            order.setOrderDate(dto.getOrderDate());
            order.setDeliveryPlace(dto.getDeliveryPlace());
            order.setStatus("0");
            order.setRemark(dto.getRemark());
            orderMapper.insert(order);

            dto.getItems().forEach(it -> it.setSoNo(soNo));
            itemMapper.batchInsert(dto.getItems());
            return soNo;
        } else {
            // ---- 修改 ----
            SaleOrder old = orderMapper.selectById(dto.getSoNo());
            if (old == null) {
                throw new BusinessException("销售单不存在");
            }
            if ("1".equals(old.getStatus())) {
                throw new BusinessException("已审核单据不能修改");
            }

            old.setCustomerId(dto.getCustomerId());
            old.setOrderDate(dto.getOrderDate());
            old.setDeliveryPlace(dto.getDeliveryPlace());
            old.setRemark(dto.getRemark());
            orderMapper.update(old);

            // 明细先清空再重建
            itemMapper.deleteBySoNo(dto.getSoNo());
            dto.getItems().forEach(it -> it.setSoNo(dto.getSoNo()));
            itemMapper.batchInsert(dto.getItems());
            return dto.getSoNo();
        }
    }

    /** 删除（已审核禁删；明细由外键 CASCADE 一并删除） */
    @Transactional
    public void delete(String soNo) {
        SaleOrder order = orderMapper.selectById(soNo);
        if (order == null) {
            throw new BusinessException("销售单不存在");
        }
        if ("1".equals(order.getStatus())) {
            throw new BusinessException("已审核单据不能删除");
        }
        orderMapper.deleteById(soNo);
    }

    /** 审核 */
    public void audit(String soNo) {
        SaleOrder order = orderMapper.selectById(soNo);
        if (order == null) {
            throw new BusinessException("销售单不存在");
        }
        if ("1".equals(order.getStatus())) {
            throw new BusinessException("该单据已审核");
        }
        orderMapper.updateStatus(soNo, "1");
    }

    /** 生成单号：XS + 制单日期(yyyyMMdd) + 3 位当天流水 */
    private String generateNo() {
        String dateStr = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String maxNo = orderMapper.selectMaxNoByDate(dateStr);
        int seq = maxNo == null ? 1 : Integer.parseInt(maxNo.substring(10)) + 1;
        return "XS" + dateStr + String.format("%03d", seq);
    }

    /** 明细合法性校验（数量、单价、折扣、商品重复） */
    private void validateItems(List<SaleItem> items) {
        Set<String> productIds = new HashSet<>();
        for (SaleItem it : items) {
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
