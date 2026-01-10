USE blog;

-- Disable foreign key checks temporarily to avoid ordering errors during insert
SET FOREIGN_KEY_CHECKS = 0;

-- --------------------------------------------------------------
-- 1. Initialize Users (Including Avatar Hashes)
-- --------------------------------------------------------------
TRUNCATE TABLE `user`;
INSERT INTO `user` (`id`, `email`, `username`, `password`, `nickname`, `avatar`, `avatar_hash`, `is_admin`, `created_at`, `updated_at`) VALUES
                                                                                                                                            (11, 'test@example.com', 'testuser', '$2a$10$pq2ntOvdhloVAGDWcUaDqenb1ApYoRLjuyjIBHvfA7t0ccpJTGkI.', 'TestUser', 'https://s2.loli.net/2026/01/08/Gv8fl7FQ9kS4ON1.jpg', 'sUCFIOjEN2iK9cWfavowGLYQHp', 0, '2025-12-29 14:38:05', '2026-01-08 21:36:55'),
                                                                                                                                            (14, '256641681@qq.com', '256', '$2a$10$L6HzpaUZsg9zDp8TPOyz8e6thEnmXJvn/KtEw7b9M4n0lGCyajine', 'RHH_PIG', 'https://s2.loli.net/2026/01/08/I9oVNrGeEJdUSz1.jpg', 'auqKpvs4rJjd6RmFOwMgiInQEz', 0, '2026-01-02 08:44:02', '2026-01-08 15:39:28');

-- --------------------------------------------------------------
-- 2. Initialize Posts (Including Cover Hashes)
-- --------------------------------------------------------------
TRUNCATE TABLE `post`;
INSERT INTO `post` (`id`, `title`, `slug`, `content`, `cover`, `cover_hash`, `category`, `published`, `deleted`, `created_at`, `updated_at`, `author_id`) VALUES
                                                                                                                                                              (1, 'Understanding Java Concurrency', 'java-concurrency', 'Content loaded. (Note: API usually returns HTML for public view. Ensure Admin API returns Markdown for editing!)', 'https://s2.loli.net/2026/01/08/a37KdxYZTjpNu8y.jpg', 'lg4hoxtmiQ8wAfvyWZcNJCL36k', 'Tech', 1, 0, '2026-01-04 12:41:28', '2026-01-08 21:22:03', 14),
                                                                                                                                                              (2, 'Docker Essentials for Developers', 'docker-essentials', '## Why Docker?\nContainerization has changed how we deploy applications...', 'https://s2.loli.net/2026/01/08/STsew4AF2a6UCd1.jpg', 'lfnPsAIoecTkQStmpiCB4ZDU7z', 'Tech', 1, 0, '2026-01-04 12:41:28', '2026-01-08 21:22:03', 14),
                                                                                                                                                              (3, 'A Weekend in Dali: Finding Peace', 'trip-to-dali', '## The Scenery\nThe Erhai lake was absolutely stunning this time of year...', 'https://s2.loli.net/2026/01/08/XeQAizU3H2tvaCh.jpg', 'f2rI9GXoyJOMviFSAduhU1blZN', 'Life', 1, 0, '2026-01-04 12:41:28', '2026-01-08 21:22:03', 14),
                                                                                                                                                              (4, 'My 2023 Reading List', 'reading-list-2023', '## Books I Read\n1. Atomic Habits\n2. Clean Code\n3. The Three-Body Problem...', 'https://s2.loli.net/2026/01/08/NcyMq2xT94hFOiB.jpg', 'lzWhsB7Lro2EVig5NtfyDeU6Ow', 'Life', 1, 0, '2026-01-04 12:41:28', '2026-01-08 21:22:03', 14),
                                                                                                                                                              (5, 'What\'s New in Spring Boot 3.0', 'spring-boot-3-new-features', '## AOT Compilation\nSpring Boot 3 introduces native image support...', 'https://s2.loli.net/2026/01/08/IxU76amDXpyhNFH.jpg', '3WUaJOktwBQc1sdySoDHnf9Z82', 'Spring', 1, 0, '2026-01-04 12:41:28', '2026-01-08 21:22:03', 14),
(6, 'Securing APIs with Spring Security 6', 'spring-security-6-guide', '## The Security Filter Chain\nGone are the days of extending WebSecurityConfigurerAdapter...', 'https://s2.loli.net/2026/01/08/9i1sp5oCL8IPTS3.jpg', 'Q8WZ3kraFCH4jPbzSmthDl5AY1', 'Spring', 1, 0, '2026-01-04 12:41:28', '2026-01-08 21:22:03', 14),
(7, 'RHH', 'rhh', '## RHH is Pig\nRhh is pig', NULL, NULL, 'Life', 1, 0, '2026-01-05 12:08:14', '2026-01-05 12:08:14', 14);

-- --------------------------------------------------------------
-- 3. Initialize Post Tags
-- --------------------------------------------------------------
TRUNCATE TABLE `post_tags`;
INSERT INTO `post_tags` (`post_id`, `tag`) VALUES
(4, 'Books'),
(2, 'DevOps'),
(2, 'Docker'),
(1, 'Java'),
(5, 'Java 17'),
(6, 'JWT'),
(1, 'Multi-threading'),
(3, 'Photography'),
(4, 'Reflection'),
(6, 'Security'),
(5, 'Spring Boot'),
(3, 'Travel');

-- --------------------------------------------------------------
-- 4. Initialize Settings (Global Config)
-- --------------------------------------------------------------
TRUNCATE TABLE `settings`;
INSERT INTO `settings` (`id`, `site_name`, `custom_background`, `background_hash`, `avatar`, `avatar_hash`, `nickname`, `bio`, `notice`, `visitor_count`, `site_start_date`, `created_at`, `updated_at`) VALUES
(1, 'LXLY Blog', NULL, NULL, './BG/icon.jpg', NULL, 'LXLY', 'Java Full Stack Developer<br>Love Technology, Love Life<br>Email: 256641681@qq.com', '👋 Welcome to LXLY\'s technical space!<br>Here I record thoughts on Java backend and system architecture.<br><strong>Latest:</strong> Backend upgraded to Spring Boot 3.0.', 1205, '2025-05-04 12:41:28', '2026-01-04 12:41:28', '2026-01-04 12:41:28');

-- --------------------------------------------------------------
-- 5. Initialize Settings Categories & Tags
-- --------------------------------------------------------------
TRUNCATE TABLE `settings_categories`;
INSERT INTO `settings_categories` (`settings_id`, `category`) VALUES
                                                                  (1, 'Life'),
                                                                  (1, 'Spring'),
                                                                  (1, 'Tech');

TRUNCATE TABLE `settings_tags`;
INSERT INTO `settings_tags` (`settings_id`, `tag`) VALUES
                                                       (1, 'Fullstack'),
                                                       (1, 'Java'),
                                                       (1, 'Travel');

-- --------------------------------------------------------------
-- 6. Initialize Verify Codes (Optional logs)
-- --------------------------------------------------------------
TRUNCATE TABLE `verify_code`;
INSERT INTO `verify_code` (`id`, `email`, `code`, `type`, `expire_at`, `used`, `created_at`) VALUES
                                                                                                 (11, '256641681@qq.com', '808367', 'register', '2026-01-02 08:53:29', 1, '2026-01-02 08:43:29'),
                                                                                                 (12, '2945706262@qq.com', '147417', 'password-reset', '2026-01-02 14:06:26', 0, '2026-01-02 13:56:26'),
                                                                                                 (13, 'ly2945706262@gmail.com', '418624', 'password-reset', '2026-01-02 14:07:01', 0, '2026-01-02 13:57:01'),
                                                                                                 (14, 'ly2945706262@gmail.com', '640518', 'register', '2026-01-04 07:39:49', 0, '2026-01-04 07:29:49'),
                                                                                                 (15, '256641681@qq.com', '517960', 'password-reset', '2026-01-04 07:42:32', 1, '2026-01-04 07:32:32'),
                                                                                                 (16, '256641681@qq.com', '559520', 'password-reset', '2026-01-04 07:44:12', 1, '2026-01-04 07:34:12'),
                                                                                                 (17, '256641681@qq.com', '345590', 'password-reset', '2026-01-04 07:53:51', 0, '2026-01-04 07:43:51');

-- Re-enable foreign key checks
SET FOREIGN_KEY_CHECKS = 1;