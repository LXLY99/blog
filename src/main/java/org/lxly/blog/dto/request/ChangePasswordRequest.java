package org.lxly.blog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class ChangePasswordRequest {
    @Email @NotBlank
    private String email;

    @NotBlank
    private String code;

    @NotBlank @Size(min = 6)
    private String newPassword;
}
