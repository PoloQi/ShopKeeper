package org.example.shopkeeper_backend.service;

import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.mapper.StockMapper;
import org.example.shopkeeper_backend.vo.StockVO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMapper stockMapper;

    public List<StockVO> list(String productName, String productId, boolean onlyPositive) {
        return stockMapper.selectStockList(productName, productId, onlyPositive);
    }
}
