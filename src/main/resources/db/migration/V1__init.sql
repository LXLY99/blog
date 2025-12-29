-- --------------------------------------------------------------
--   1️⃣ 创建业务库（如果库已经存在可以省略）
-- --------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog;


-- --------------------------------------------------------------
--   2.1 用户表（唯一约束：email / username）
-- --------------------------------------------------------------
CREATE TABLE `user` (
                        `id`          BIGINT      NOT NULL AUTO_INCREMENT,
                        `email`       VARCHAR(255) NOT NULL,
                        `username`    VARCHAR(50)  NOT NULL,
                        `password`    VARCHAR(255) NOT NULL,
                        `nickname`    VARCHAR(50)  NULL,
                        `avatar`      VARCHAR(255) NULL,
                        `is_admin`    BIT          NOT NULL DEFAULT 0 COMMENT '0=普通用户 1=管理员',
                        `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_user_email`    (`email`),
                        UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户（包括管理员）';

-- --------------------------------------------------------------
--   2.2 文章表（支持软删、发布、分类、封面、作者外键）
-- --------------------------------------------------------------
CREATE TABLE `post` (
                        `id`          BIGINT      NOT NULL AUTO_INCREMENT,
                        `title`       VARCHAR(255) NOT NULL,
                        `slug`        VARCHAR(255) NOT NULL,
                        `content`     MEDIUMTEXT   NOT NULL,
                        `cover`       VARCHAR(255) NULL,
                        `category`    VARCHAR(100) NULL,
                        `published`   BIT          NOT NULL DEFAULT 0 COMMENT '0=草稿 1=已发布',
                        `deleted`     BIT          NOT NULL DEFAULT 0 COMMENT '软删标记，0=有效 1=已删',
                        `created_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at`  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        `author_id`   BIGINT       NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_post_slug` (`slug`),
                        KEY `idx_post_category` (`category`),
                        KEY `idx_post_published` (`published`),
                        KEY `idx_post_deleted` (`deleted`),
                        CONSTRAINT `fk_post_user` FOREIGN KEY (`author_id`)
                            REFERENCES `user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客文章（支持软删）';


-- --------------------------------------------------------------
--   2.3 文章‑标签关联表（多对多，tag 直接存字符串）
-- --------------------------------------------------------------
CREATE TABLE `post_tags` (
                             `post_id` BIGINT NOT NULL,
                             `tag`     VARCHAR(50) NOT NULL,
                             PRIMARY KEY (`post_id`,`tag`),
                             CONSTRAINT `fk_post_tags_post` FOREIGN KEY (`post_id`)
                                 REFERENCES `post`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                             KEY `idx_tag` (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签（多对多）';

-- --------------------------------------------------------------
--   2.4 站点全局设置（单例行）
-- --------------------------------------------------------------
CREATE TABLE `settings` (
                            `id`                BIGINT      NOT NULL AUTO_INCREMENT,
                            `site_name`         VARCHAR(100) NULL,
                            `custom_background` VARCHAR(255) NULL,
                            `avatar`            VARCHAR(255) NULL,
                            `nickname`          VARCHAR(50)  NULL,
                            `bio`               TEXT        NULL,
                            `notice`            TEXT        NULL COMMENT '公告或 About 页面内容（Markdown -> HTML）',
                            `visitor_count`     BIGINT      NOT NULL DEFAULT 0,
                            `site_start_date`   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `created_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_at`        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站点全局配置（单例记录）';

-- --------------------------------------------------------------
--   2.5 settings_categories（Settings 表的分类集合）
-- --------------------------------------------------------------
CREATE TABLE `settings_categories` (
                                       `settings_id` BIGINT NOT NULL,
                                       `category`    VARCHAR(100) NOT NULL,
                                       PRIMARY KEY (`settings_id`,`category`),
                                       CONSTRAINT `fk_cat_settings` FOREIGN KEY (`settings_id`)
                                           REFERENCES `settings`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                       KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站点分类集合（存放所有分类) ';


-- --------------------------------------------------------------
--   2.6 settings_tags（Settings 表的标签集合）
-- --------------------------------------------------------------
CREATE TABLE `settings_tags` (
                                 `settings_id` BIGINT NOT NULL,
                                 `tag`         VARCHAR(100) NOT NULL,
                                 PRIMARY KEY (`settings_id`,`tag`),
                                 CONSTRAINT `fk_tag_settings` FOREIGN KEY (`settings_id`)
                                     REFERENCES `settings`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                 KEY `idx_tag` (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站点标签集合（全局标签) ';


-- --------------------------------------------------------------
--   2.7 邮箱验证码表（用于注册、找回密码等场景）
-- --------------------------------------------------------------
CREATE TABLE `verify_code` (
                               `id`         BIGINT      NOT NULL AUTO_INCREMENT,
                               `email`      VARCHAR(255) NOT NULL,
                               `code`       VARCHAR(6)   NOT NULL,
                               `type`       VARCHAR(20)  NOT NULL COMMENT 'register / password-reset',
                               `expire_at`  DATETIME    NOT NULL,
                               `used`       BIT         NOT NULL DEFAULT 0 COMMENT '0=未使用 1=已使用',
                               `created_at` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               KEY `idx_verify_email_type` (`email`,`type`),
                               KEY `idx_verify_expire` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邮件验证码（一次性）';

