package com.health.assessment.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 认证异常
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误代码（默认 401）
     */
    private Integer code;

    /**
     * 错误消息
     */
    private String message;

    public AuthenticationException(String message) {
        super(message);
        this.code = 401;
        this.message = message;
    }

    public AuthenticationException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

}

