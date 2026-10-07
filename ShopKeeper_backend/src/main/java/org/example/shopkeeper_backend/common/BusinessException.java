package org.example.shopkeeper_backend.common;

import lombok.Getter;

/**
 * 业务异常（如已审核单据禁止修改、违反完整性约束等）
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String message) {
        this(400, message);
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
