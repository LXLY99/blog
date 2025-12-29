package org.lxly.blog.dto.response;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Result<T> {
    /** 0 表示成功，>0 业务错误，<0 系统错误 */
    private int code;
    private String message;
    private T data;

    public static <T> Result<T> ok(T data) {
        return new Result<>(0, "OK", data);
    }

    public static Result<Void> ok() {
        return new Result<>(0, "OK", null);
    }

    public static Result<Void> fail(int code, String msg) {
        return new Result<>(code, msg, null);
    }

    public static Result<Void> fail(String msg) {
        return fail(1, msg);
    }
}
