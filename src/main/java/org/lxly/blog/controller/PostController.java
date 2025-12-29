package org.lxly.blog.controller;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.*;
import org.lxly.blog.dto.request.PostDtor;
import org.lxly.blog.dto.response.PostDtos;
import org.lxly.blog.dto.response.*;
import org.lxly.blog.service.PostService;
import org.lxly.blog.config.JwtUtil;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;

    /** 首页 / 归档 / 列表：返回已发布文章（不包含正文） */
    @GetMapping("/posts")
    public ResponseEntity<Result<List<PostDtos>>> listPosts() {
        List<PostDtos> posts = postService.listAll();
        return ResponseEntity.ok(Result.ok(posts));
    }

    /** 文章详情（slug） */
    @GetMapping("/post/{slug}")
    public ResponseEntity<Result<PostDetailDto>> getPost(@PathVariable String slug) {
        PostDetailDto dto = postService.getBySlug(slug);
        return ResponseEntity.ok(Result.ok(dto));
    }

    /** 首页右侧统计 */
    @GetMapping("/stats")
    public ResponseEntity<Result<SiteStatsDto>> siteStats() {
        SiteStatsDto dto = postService.stats();
        return ResponseEntity.ok(Result.ok(dto));
    }

    /** 新建或编辑文章（仅管理员） */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/post")
    public ResponseEntity<Result<Void>> createOrUpdate(
            @Valid @RequestBody PostDtor dto,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        // ① 通过 Spring Security 上下文获取 userId
        Long userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                userId = (Long) principal;
            }
        }

        // ② 若 SecurityContext 没有（极少情况），手动解析 JWT
        if (userId == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseToken(token);
            userId = Long.valueOf(claims.getSubject());
        }

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(401, "未能获取登录用户信息"));
        }

        postService.createOrUpdate(dto, userId);
        return ResponseEntity.ok(Result.ok(null));
    }

    /** 删除文章（软删） */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/post/{id}")
    public ResponseEntity<Result<Void>> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.ok(Result.ok(null));
    }
}
