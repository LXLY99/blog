package org.lxly.blog.dto.response;

import lombok.*;
import org.lxly.blog.entity.User;

import java.time.LocalDateTime;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class UserInfoDto {
    private Long id;
    private String email;
    private String username;
    private String nickname;
    private String avatar;
    private Boolean isAdmin;
    private LocalDateTime createdAt;

    public static UserInfoDto from(User u) {
        return UserInfoDto.builder()
                .id(u.getId())
                .email(u.getEmail())
                .username(u.getUsername())
                .nickname(u.getNickname())
                .avatar(u.getAvatar())
                .isAdmin(u.getIsAdmin())
                .createdAt(u.getCreatedAt())
                .build();
    }
}
