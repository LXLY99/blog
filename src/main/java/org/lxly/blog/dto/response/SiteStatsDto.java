package org.lxly.blog.dto.response;

import lombok.*;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class SiteStatsDto {
    private Long postCount;
    private Long categoryCount;
    private Long tagCount;
    private Long totalWords;
    private Long visitorCount;
    private java.time.LocalDateTime siteStartDate;
}
