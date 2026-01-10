package org.lxly.blog.service;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.lxly.blog.dto.response.PostDetailDto;
import org.lxly.blog.dto.response.SiteStatsDto;
import org.lxly.blog.dto.request.PostRequest;
import org.lxly.blog.dto.response.PostSummaryDto;
import org.lxly.blog.entity.*;
import org.lxly.blog.mapper.*;
import org.lxly.blog.repository.*;
import org.lxly.blog.util.MarkdownUtil;
import org.springframework.beans.factory.annotation.*;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.*;
import org.springframework.transaction.annotation.*;

import java.time.*;
import java.util.*;
import java.util.stream.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepo;
    private final SettingsRepository settingsRepo;
    private final PostMapper postMapper;
    private final MarkdownUtil markdownUtil;
    private final SmmsService smmsService;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate; // 若未开启 Redis 为 null

    /** 首页/归档/列表：已发布且未软删的文章（不含正文） */
    public List<PostSummaryDto> listAll() {
        List<Post> posts = postRepo.findAllByPublishedTrueOrderByCreatedAtDesc();
        return posts.stream()
                .map(postMapper::toDto)
                .collect(Collectors.toList());
    }

    /** 根据 slug 查询文章详情（如果已软删则抛错） */
    public PostDetailDto getBySlug(String slug) {
        Post post = postRepo.findBySlugAndDeletedFalse(slug)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        // 先尝试 Redis 缓存
        String cacheKey = "post:html:" + slug;
        String html = null;
        if (redisTemplate != null) {
            html = redisTemplate.opsForValue().get(cacheKey);
        }
        if (html == null) {
            html = markdownUtil.render(post.getContent());
            if (redisTemplate != null) {
                redisTemplate.opsForValue().set(cacheKey, html, Duration.ofHours(12));
            }
        }
        return postMapper.toDetailDto(post, html);
    }

    /** 新增或编辑文章（管理员） */
    @Transactional
    public Post createOrUpdate(PostRequest dto, Long authorId) {
        Post post;

        // 1. Determine if Create or Update
        if (dto.getId() == null) {
            // --- CREATE CASE ---
            post = new Post();
            post.setCreatedAt(LocalDateTime.now());

            // Set Author only on Create
            User author = new User();
            author.setId(authorId);
            post.setAuthor(author);

        } else {
            // --- UPDATE CASE ---
            post = postRepo.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
            // ✅ Logic: If new cover is provided AND it differs from old cover
            if (dto.getCover() != null && !dto.getCover().equals(post.getCover())) {
                // Delete the OLD cover using the OLD hash stored in DB
                String oldCover = post.getCover();
                String newCover = dto.getCover();
                smmsService.delete(post.getCoverHash(), oldCover);
            }
        }

        // 2. Set Common Fields
        post.setTitle(dto.getTitle());
        post.setSlug(dto.getSlug() != null && !dto.getSlug().isEmpty() ? dto.getSlug() : generateSlug(dto.getTitle()));
        post.setContent(dto.getContent());
        post.setCover(dto.getCover());
        post.setCoverHash(dto.getCoverHash());
        post.setCategory(dto.getCategory());
        post.setTags(dto.getTags() == null ? new HashSet<>() : dto.getTags());
        post.setPublished(dto.getPublished());
        post.setUpdatedAt(LocalDateTime.now());

        // 3. Save to DB
        Post saved = postRepo.save(post);

        // 4. Clear Cache
        if (redisTemplate != null) {
            // Safe delete in case redis is down
            try {
                redisTemplate.delete("post:html:" + saved.getSlug());
                redisTemplate.delete("site:stats");
            } catch (Exception e) {
                log.error("Redis delete failed", e);
            }
        }

        return saved;
    }

    /** 删除文章（软删） */
    @Transactional
    public void delete(Long id) {
        Post post = postRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        post.setDeleted(true);
        postRepo.save(post);
        if (redisTemplate != null) {
            redisTemplate.delete("post:html:" + post.getSlug());
            redisTemplate.delete("site:stats");
        }
    }

    /** 首页右侧统计 */
    public SiteStatsDto stats() {
        long postCount = postRepo.count();
        long totalWords = postRepo.findAll()
                .stream()
                .mapToLong(p -> p.getContent().length())
                .sum();
        Settings s = settingsRepo.findAll().stream().findFirst().orElse(new Settings());

        return SiteStatsDto.builder()
                .postCount(postCount)
                .categoryCount(s.getCategories() == null ? 0L : s.getCategories().size())
                .tagCount(s.getTags() == null ? 0L : s.getTags().size())
                .totalWords(totalWords)
                .visitorCount(s.getVisitorCount())
                .siteStartDate(s.getSiteStartDate())
                .build();
    }

    /** 简单的 slug 生成 */
    private String generateSlug(String title) {
        // 1. 转小写，非字母数字换成横杠
        String slug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\u4e00-\\u9fa5]+", "-") // 支持中文标题的拼音或直接保留中文（视需求）
                .replaceAll("^-|-$", "");

        // 2. 如果为空（比如全是特殊符号），兜底
        if (slug.isBlank()) {
            slug = "post-" + UUID.randomUUID().toString().substring(0, 8);
        }

        // 3. 检查是否重复
        if (postRepo.findBySlug(slug).isPresent()) {
            // 重复了再加后缀
            return slug + "-" + System.currentTimeMillis();
        }

        return slug;
    }

    public List<PostSummaryDto> listAllForAdmin() {
        List<Post> posts = postRepo.findAllByDeletedFalseOrderByCreatedAtDesc();
        return posts.stream()
                .map(postMapper::toDto)
                .collect(Collectors.toList());
    }

}
