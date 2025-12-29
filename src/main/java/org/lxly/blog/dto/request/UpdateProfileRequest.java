package org.lxly.blog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class UpdateProfileRequest {
    @NotBlank
    private String username;

    private String nickname;

    private String avatar; // 完整 URL
}
