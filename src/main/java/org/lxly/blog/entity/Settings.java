package org.lxly.blog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "`settings`")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class Settings {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_name", length = 100)
    private String siteName;

    @Column(name = "custom_background", length = 255)
    private String customBackground;

    @Column(length = 255)
    private String avatar;

    @Column(length = 50)
    private String nickname;

    @Lob
    private String bio;

    @Lob
    private String notice;  // 公告或 about 内容（Markdown / HTML）

    /** 分类集合（JSON / ElementCollection） */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "settings_categories", joinColumns = @JoinColumn(name = "settings_id"))
    @Column(name = "category")
    private Set<String> categories = new HashSet<>();

    /** 标签集合 */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "settings_tags", joinColumns = @JoinColumn(name = "settings_id"))
    @Column(name = "tag")
    private Set<String> tags = new HashSet<>();

    @Column(name = "visitor_count", nullable = false)
    private Long visitorCount = 0L;

    @Column(name = "site_start_date")
    private LocalDateTime siteStartDate = LocalDateTime.now();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
