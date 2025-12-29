package org.lxly.blog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class RegisterRequest {
    @Email @NotBlank
    private String email;

    @NotBlank @Size(min = 3, max = 20)
    private String username;

    @NotBlank @Size(min = 6)
    private String password;

    @NotBlank
    private String code;   // 验证码
}
