package org.example.shopkeeper_backend.controller;

import lombok.RequiredArgsConstructor;
import org.example.shopkeeper_backend.common.Result;
import org.example.shopkeeper_backend.service.StockService;
import org.example.shopkeeper_backend.vo.StockVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;

    /** 实时库存查询（数据来自 v_stock 视图） */
    @GetMapping
    public Result<List<StockVO>> list(
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String productId,
            @RequestParam(defaultValue = "false") boolean onlyPositive) {
        return Result.ok(stockService.list(productName, productId, onlyPositive));
    }
}
