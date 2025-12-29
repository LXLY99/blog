package org.lxly.blog.dto.response;

import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class PostDtos {
    private Long id;
    private String title;
    private String slug;
    private String cover;
    private String category;
    private Set<String> tags;
    private LocalDateTime createdAt;
    private Boolean published;
}
