package org.lxly.blog.dto.response;

import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class AboutDto {
    private String title;
    private String content; // 已渲染的 HTML（或原始 Markdown，由后端渲染后返回）
}
