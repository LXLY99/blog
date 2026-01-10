package org.lxly.blog.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 文章列表项 DTO (仅包含摘要信息，不含正文)
 * 原名: PostDtos (建议重命名为 PostSummaryDto)
 */
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class PostSummaryDto {
    private Long id;
    private String title;
    private String slug;
    private String cover;
    private String category;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private Boolean published;

}