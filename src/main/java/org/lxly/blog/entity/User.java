package org.lxly.blog.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 站点用户（包括普通用户和管理员）。
 * 所有列均使用 JPA 注解，避免与 Hibernate @Table 冲突。
 */
@Entity
/* 只使用 JPA 的 Table 注解，表名为 `user`（MySQL 关键字，用反引号包裹） */
@Table(name = "`user`")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 必须唯一的邮箱 */
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** 必须唯一的用户名（登录名） */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /** BCrypt 加密后的密码 */
    @Column(nullable = false)
    private String password;

    /** 显示在页面上的昵称，默认使用 username */
    @Column(length = 50)
    private String nickname;

    /** 头像 URL（可为空） */
    @Column(length = 255)
    private String avatar;

    /** 是否为管理员（超级权限） */
    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin = false;

    /** 注册时间（自动填充） */
    @CreationTimestamp
    private LocalDateTime createdAt;

    /** 最近一次更新的时间（自动更新） */
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
