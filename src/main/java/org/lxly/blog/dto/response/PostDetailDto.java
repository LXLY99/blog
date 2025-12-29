package org.lxly.blog.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class PostDetailDto {
    private Long id;
    private String title;
    private String slug;
    private String contentHtml;   // 已渲染的 HTML
    private String cover;
    private String category;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private String authorName;
    private String authorAvatar;
}
