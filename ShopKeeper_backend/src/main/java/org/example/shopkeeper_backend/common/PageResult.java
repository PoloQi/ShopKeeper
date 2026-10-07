package org.example.shopkeeper_backend.common;

import lombok.Data;

import java.util.List;

/**
 * 分页返回结构
 */
@Data
public class PageResult<T> {

    private Long total;
    private List<T> records;

    public PageResult(Long total, List<T> records) {
        this.total = total;
        this.records = records;
    }
}
