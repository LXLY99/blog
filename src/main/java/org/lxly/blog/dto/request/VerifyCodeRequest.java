package org.lxly.blog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
public class VerifyCodeRequest {
    @Email @NotBlank
    private String email;

    @NotBlank
    private String type;   // 前端传 "register" 或 "password-reset"
}
