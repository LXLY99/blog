package org.lxly.blog.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * 文章发布/更新请求
 * 原名: PostDtor (建议重命名为 PostRequest)
 */
@Getter @Setter
public class PostRequest {

    private Long id; // null → 新建，非 null → 编辑

    @NotBlank(message = "文章标题不能为空")
    @Size(max = 100, message = "标题过长")
    private String title;

    @Pattern(regexp = "^[a-zA-Z0-9-_]*$", message = "Slug 只能包含字母、数字、横杠和下划线")
    private String slug; // 若为空系统自动生成

    @NotBlank(message = "文章内容不能为空")
    private String content; // Markdown

    private String cover; // 图片 URL

    private String coverHash;

    private String category;

    // 默认初始化防止 NPE
    private Set<String> tags = new HashSet<>();

    private Boolean published = false;
}