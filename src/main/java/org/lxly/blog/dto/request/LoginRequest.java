package org.lxly.blog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class LoginRequest {
    @Email @NotBlank
    private String email;

    @NotBlank
    private String password;
}
