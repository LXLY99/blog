package org.lxly.blog.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lxly.blog.entity.Post;
import org.lxly.blog.entity.Settings;
import org.lxly.blog.entity.User;
import org.lxly.blog.repository.PostRepository;
import org.lxly.blog.repository.SettingsRepository;
import org.lxly.blog.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Data Initializer
 * Automatically inserts test data into the database on startup if it is empty.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SettingsRepository settingsRepository;
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("🚀 Starting Data Initialization...");
        User admin = initAdminUser();
        initSettings();
        initPosts(admin);
        log.info("✅ Data Initialization Completed!");
    }

    /**
     * 1. Initialize Admin User (Webmaster)
     */
    private User initAdminUser() {
        String email = "256641681@qq.com"; // Your specific email
        return userRepository.findByEmail(email).orElseGet(() -> {
            log.info("Creating Admin User: {}", email);
            User user = User.builder()
                    .email(email)
                    .username("LXLY")
                    .nickname("LXLY")
                    // Default password: 123456 (Encoded)
                    .password(passwordEncoder.encode("123456"))
                    .isAdmin(true)
                    .avatar("./BG/icon.jpg")
                    .createdAt(LocalDateTime.now())
                    .build();
            return userRepository.save(user);
        });
    }

    /**
     * 2. Initialize Site Settings (Founder Profile)
     */
    private void initSettings() {
        if (settingsRepository.count() == 0) {
            log.info("Creating Default Site Settings...");

            // Define Categories
            Set<String> categories = new HashSet<>();
            categories.add("Tech");
            categories.add("Life");
            categories.add("Spring");

            // Define Global Tags
            Set<String> tags = new HashSet<>();
            tags.add("Java");
            tags.add("Fullstack");
            tags.add("Travel");

            Settings settings = Settings.builder()
                    .siteName("LXLY Blog")
                    .nickname("LXLY")
                    .avatar("./BG/icon.jpg")
                    .bio("Java Full Stack Developer<br>Love Technology, Love Life<br>Email: 256641681@qq.com")
                    .notice("👋 Welcome to LXLY's technical space!<br>Here I record thoughts on Java backend and system architecture.<br><strong>Latest:</strong> Backend upgraded to Spring Boot 3.0.")
                    .visitorCount(1205L)
                    .categories(categories)
                    .tags(tags)
                    .siteStartDate(LocalDateTime.now().minusMonths(8))
                    .build();

            settingsRepository.save(settings);
        }
    }

    /**
     * 3. Initialize Blog Posts (2 per Category)
     */
    private void initPosts(User author) {
        if (postRepository.count() == 0) {
            log.info("Inserting Sample Blog Posts...");

            // --- Category: Tech ---

            // Post 1
            createPost(author,
                    "Understanding Java Concurrency",
                    "java-concurrency",
                    "## CompletableFuture Guide\nIn modern Java development, asynchronous programming is key...",
                    "Tech",
                    "https://images.unsplash.com/photo-1518770660439-4636190af475?w=500&auto=format&fit=crop&q=60",
                    Set.of("Java", "Multi-threading")
            );

            // Post 2
            createPost(author,
                    "Docker Essentials for Developers",
                    "docker-essentials",
                    "## Why Docker?\nContainerization has changed how we deploy applications...",
                    "Tech",
                    "https://images.unsplash.com/photo-1605745341112-85968b19335b?w=500&auto=format&fit=crop&q=60",
                    Set.of("DevOps", "Docker")
            );

            // --- Category: Life ---

            // Post 3
            createPost(author,
                    "A Weekend in Dali: Finding Peace",
                    "trip-to-dali",
                    "## The Scenery\nThe Erhai lake was absolutely stunning this time of year...",
                    "Life",
                    "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?w=500&auto=format&fit=crop&q=60",
                    Set.of("Travel", "Photography")
            );

            // Post 4
            createPost(author,
                    "My 2023 Reading List",
                    "reading-list-2023",
                    "## Books I Read\n1. Atomic Habits\n2. Clean Code\n3. The Three-Body Problem...",
                    "Life",
                    null, // No cover image
                    Set.of("Books", "Reflection")
            );

            // --- Category: Spring ---

            // Post 5
            createPost(author,
                    "What's New in Spring Boot 3.0",
                    "spring-boot-3-new-features",
                    "## AOT Compilation\nSpring Boot 3 introduces native image support...",
                    "Spring",
                    "https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=500&auto=format&fit=crop&q=60",
                    Set.of("Spring Boot", "Java 17")
            );

            // Post 6
            createPost(author,
                    "Securing APIs with Spring Security 6",
                    "spring-security-6-guide",
                    "## The Security Filter Chain\nGone are the days of extending WebSecurityConfigurerAdapter...",
                    "Spring",
                    "https://images.unsplash.com/photo-1562813733-b31f71025d54?w=500&auto=format&fit=crop&q=60",
                    Set.of("Security", "JWT")
            );
        }
    }

    // Helper method to create and save a post
    private void createPost(User author, String title, String slug, String content, String category, String cover, Set<String> tags) {
        Post post = Post.builder()
                .title(title)
                .slug(slug)
                .content(content)
                .category(category)
                .cover(cover)
                .tags(new HashSet<>(tags))
                .author(author)
                .published(true)
                .deleted(false)
                .createdAt(LocalDateTime.now().minusDays((long) (Math.random() * 30))) // Random date in last 30 days
                .build();
        postRepository.save(post);
    }
}