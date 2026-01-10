package org.lxly.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxly.blog.dto.request.PostRequest;
import org.lxly.blog.dto.response.PostDetailDto;
import org.lxly.blog.dto.response.PostSummaryDto;
import org.lxly.blog.dto.response.Result;
import org.lxly.blog.dto.response.SiteStatsDto;
import org.lxly.blog.service.PostService;
import org.lxly.blog.service.SmmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <h1>文章与内容管理模块 (Controller Layer)</h1>
 * <p>
 * 负责博客文章的发布、查询、展示以及站点统计数据的获取。
 * </p>
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * 4.1 文章列表/归档
     */
    @GetMapping("/posts")
    public ResponseEntity<Result<List<PostSummaryDto>>> listPosts() {
        return ResponseEntity.ok(Result.ok(postService.listAll()));
    }

    /**
     * 4.2 文章详情
     */
    @GetMapping("/post/{slug}")
    public ResponseEntity<Result<PostDetailDto>> getPost(@PathVariable String slug) {
        return ResponseEntity.ok(Result.ok(postService.getBySlug(slug)));
    }

    /**
     * 4.3 站点统计
     */
    @GetMapping("/stats")
    public ResponseEntity<Result<SiteStatsDto>> siteStats() {
        return ResponseEntity.ok(Result.ok(postService.stats()));
    }

    /**
     * <h2>4.4 新建或编辑文章 (Create or Update Post)</h2>
     * <p>
     * 权限级别: 需认证 (Authenticated) - 允许所有登录用户发布
     * </p>
     */
    // Change from hasRole('ADMIN') to isAuthenticated()
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/admin/post")
    public ResponseEntity<Result<Void>> createOrUpdate(@Valid @RequestBody PostRequest dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = (Long) authentication.getPrincipal();

        postService.createOrUpdate(dto, userId);
        return ResponseEntity.ok(Result.ok(null));
    }

    /**
     * 4.5 删除文章
     * 权限级别: 需认证 (Authenticated)
     */
    // Change from hasRole('ADMIN') to isAuthenticated()
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/admin/post/{id}")
    public ResponseEntity<Result<Void>> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.ok(Result.ok(null));
    }

    /**
     * 4.6 管理端获取所有文章列表
     * 权限级别: 需认证 (Authenticated)
     */
    // Change from hasRole('ADMIN') to isAuthenticated()
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/admin/posts")
    public ResponseEntity<Result<List<PostSummaryDto>>> listAllPostsForAdmin() {
        List<PostSummaryDto> posts = postService.listAllForAdmin();
        return ResponseEntity.ok(Result.ok(posts));
    }
}