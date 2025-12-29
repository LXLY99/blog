package org.lxly.blog.dto.response;

import lombok.*;

import java.util.Set;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class SettingsDto {
    private String siteName;
    private String customBackground;
    private String avatar;
    private String nickname;
    private String bio;
    private String notice;
    private Set<String> categories;
    private Set<String> tags;
    private Long visitorCount;
    private java.time.LocalDateTime siteStartDate;
}
