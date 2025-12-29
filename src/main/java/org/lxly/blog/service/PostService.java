package org.lxly.blog.service;

import lombok.*;
import org.lxly.blog.dto.response.PostDetailDto;
import org.lxly.blog.dto.response.SiteStatsDto;
import org.lxly.blog.dto.request.PostDtor;
import org.lxly.blog.dto.response.PostDtos;
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
public class PostService {

    private final PostRepository postRepo;
    private final SettingsRepository settingsRepo;
    private final PostMapper postMapper;
    private final MarkdownUtil markdownUtil;

    @Autowired(required = false)
    private RedisTemplate<String, String> redisTemplate; // 若未开启 Redis 为 null

    /** 首页/归档/列表：已发布且未软删的文章（不含正文） */
    public List<PostDtos> listAll() {
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
    public Post createOrUpdate(PostDtor dto, Long authorId) {
        Post post;
        if (dto.getId() == null) {
            post = new Post();
            post.setCreatedAt(LocalDateTime.now());
        } else {
            post = postRepo.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("文章不存在"));
        }
        post.setTitle(dto.getTitle());
        post.setSlug(dto.getSlug() != null ? dto.getSlug() : generateSlug(dto.getTitle()));
        post.setContent(dto.getContent());
        post.setCover(dto.getCover());
        post.setCategory(dto.getCategory());
        post.setTags(dto.getTags() == null ? new HashSet<>() : dto.getTags());
        post.setPublished(dto.getPublished());
        post.setUpdatedAt(LocalDateTime.now());

        // 设置作者（只保存 id，懒加载）
        User author = new User();
        author.setId(authorId);
        post.setAuthor(author);

        Post saved = postRepo.save(post);

        // 清除相关缓存
        if (redisTemplate != null) {
            redisTemplate.delete("post:html:" + saved.getSlug());
            redisTemplate.delete("site:stats");
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
        String base = title.toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return base + "-" + System.currentTimeMillis();
    }
}
