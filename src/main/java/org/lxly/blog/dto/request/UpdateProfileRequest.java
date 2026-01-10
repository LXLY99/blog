package org.lxly.blog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class UpdateProfileRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username; // 如果允许修改用户名

    @Size(max = 20, message = "昵称不能超过20个字符")
    private String nickname;

    @Size(max = 255, message = "头像链接过长")
    private String avatar; // 完整 URL
}