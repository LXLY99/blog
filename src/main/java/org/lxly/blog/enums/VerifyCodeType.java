package org.lxly.blog.enums;

import com.fasterxml.jackson.annotation.JsonValue;

public enum VerifyCodeType {
    REGISTER("register"),
    PASSWORD_RESET("password-reset");

    private final String value;

    VerifyCodeType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}
