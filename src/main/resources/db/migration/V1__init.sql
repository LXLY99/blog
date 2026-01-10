-- 1. Create and Select Database
CREATE DATABASE IF NOT EXISTS blog CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog;

-- 2. User Table (Includes avatar_hash for SMMS deletion)
CREATE TABLE `user` (
                        `id`          BIGINT       NOT NULL AUTO_INCREMENT,
                        `email`       VARCHAR(255) NOT NULL,
                        `username`    VARCHAR(50)  NOT NULL,
                        `password`    VARCHAR(255) NOT NULL,
                        `nickname`    VARCHAR(50)  NULL,
                        `avatar`      VARCHAR(255) NULL,
                        `avatar_hash` VARCHAR(100) NULL COMMENT 'SMMS Deletion Hash',
                        `is_admin`    BIT          NOT NULL DEFAULT 0 COMMENT '0=User 1=Admin',
                        `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_user_email`    (`email`),
                        UNIQUE KEY `uk_user_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Post Table (Includes cover_hash for SMMS deletion)
CREATE TABLE `post` (
                        `id`          BIGINT       NOT NULL AUTO_INCREMENT,
                        `title`       VARCHAR(255) NOT NULL,
                        `slug`        VARCHAR(255) NOT NULL,
                        `content`     MEDIUMTEXT   NOT NULL,
                        `cover`       VARCHAR(255) NULL,
                        `cover_hash`  VARCHAR(100) NULL COMMENT 'SMMS Deletion Hash',
                        `category`    VARCHAR(100) NULL,
                        `published`   BIT          NOT NULL DEFAULT 0 COMMENT '0=Draft 1=Published',
                        `deleted`     BIT          NOT NULL DEFAULT 0 COMMENT '0=Valid 1=Deleted',
                        `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                        `author_id`   BIGINT       NULL,
                        PRIMARY KEY (`id`),
                        UNIQUE KEY `uk_post_slug` (`slug`),
                        KEY `idx_post_category` (`category`),
                        KEY `idx_post_published` (`published`),
                        KEY `idx_post_deleted` (`deleted`),
                        CONSTRAINT `fk_post_user` FOREIGN KEY (`author_id`)
                            REFERENCES `user`(`id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Post Tags (Many-to-Many)
CREATE TABLE `post_tags` (
                             `post_id` BIGINT      NOT NULL,
                             `tag`     VARCHAR(50) NOT NULL,
                             PRIMARY KEY (`post_id`,`tag`),
                             CONSTRAINT `fk_post_tags_post` FOREIGN KEY (`post_id`)
                                 REFERENCES `post`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                             KEY `idx_tag` (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Settings Table (Global Config with Hash support)
CREATE TABLE `settings` (
                            `id`                BIGINT       NOT NULL AUTO_INCREMENT,
                            `site_name`         VARCHAR(100) NULL,
                            `custom_background` VARCHAR(255) NULL,
                            `background_hash`   VARCHAR(100) NULL COMMENT 'SMMS Hash for BG',
                            `avatar`            VARCHAR(255) NULL,
                            `avatar_hash`       VARCHAR(100) NULL COMMENT 'SMMS Hash for Site Avatar',
                            `nickname`          VARCHAR(50)  NULL,
                            `bio`               TEXT         NULL,
                            `notice`            TEXT         NULL,
                            `visitor_count`     BIGINT       NOT NULL DEFAULT 0,
                            `site_start_date`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `created_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            `updated_at`        DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Settings Categories
CREATE TABLE `settings_categories` (
                                       `settings_id` BIGINT       NOT NULL,
                                       `category`    VARCHAR(100) NOT NULL,
                                       PRIMARY KEY (`settings_id`,`category`),
                                       CONSTRAINT `fk_cat_settings` FOREIGN KEY (`settings_id`)
                                           REFERENCES `settings`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                       KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Settings Tags
CREATE TABLE `settings_tags` (
                                 `settings_id` BIGINT       NOT NULL,
                                 `tag`         VARCHAR(100) NOT NULL,
                                 PRIMARY KEY (`settings_id`,`tag`),
                                 CONSTRAINT `fk_tag_settings` FOREIGN KEY (`settings_id`)
                                     REFERENCES `settings`(`id`) ON DELETE CASCADE ON UPDATE CASCADE,
                                 KEY `idx_tag` (`tag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 8. Verify Code Table (Email Verification)
CREATE TABLE `verify_code` (
                               `id`         BIGINT       NOT NULL AUTO_INCREMENT,
                               `email`      VARCHAR(255) NOT NULL,
                               `code`       VARCHAR(6)   NOT NULL,
                               `type`       VARCHAR(20)  NOT NULL,
                               `expire_at`  DATETIME     NOT NULL,
                               `used`       BIT          NOT NULL DEFAULT 0,
                               `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (`id`),
                               KEY `idx_verify_email_type` (`email`,`type`),
                               KEY `idx_verify_expire` (`expire_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;