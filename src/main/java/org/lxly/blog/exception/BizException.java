package org.lxly.blog.exception;

/**
 * 业务异常，统一使用错误码。
 */
public class BizException extends RuntimeException {

    private final int code;

    /**
     * 通用业务异常（默认错误码 4000 或 5000）
     * 修复：AuthService 中只传 message 的情况
     */
    public BizException(String message) {
        super(message);
        this.code = 4000; // 默认业务错误码
    }

    /**
     * 指定错误码的异常
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}