package com.health.assessment.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 资源不存在异常
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误代码（默认 404）
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    public ResourceNotFoundException(String message) {
        super(message);
        this.code = 404;
        this.message = message;
    }

    public ResourceNotFoundException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

}

