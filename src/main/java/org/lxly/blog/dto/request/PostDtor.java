package org.lxly.blog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Getter @Setter
public class PostDtor {
    private Long id;                         // null → 新建，非 null → 编辑
    @NotBlank
    private String title;
    private String slug;                     // 若为空系统自动生成
    @NotBlank
    private String content;                  // Markdown
    private String cover;                    // 图片 URL
    private String category;
    private Set<String> tags = new HashSet<>();
    private Boolean published = false;
}
