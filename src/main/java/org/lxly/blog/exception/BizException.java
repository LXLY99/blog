package org.lxly.blog.exception;

/**
 * 业务异常，统一使用错误码。
 */
public class BizException extends RuntimeException {
    private final int code;

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
