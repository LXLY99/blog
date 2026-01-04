package org.lxly.blog.controller;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxly.blog.config.JwtUtil;
import org.lxly.blog.dto.request.PostDtor;
import org.lxly.blog.dto.response.PostDetailDto;
import org.lxly.blog.dto.response.PostDtos;
import org.lxly.blog.dto.response.Result;
import org.lxly.blog.dto.response.SiteStatsDto;
import org.lxly.blog.service.PostService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
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
 * 包含面向访客的公开接口和面向管理员的管理接口。
 * </p>
 *
 * <ul>
 * <li><strong>基础路径:</strong> /api</li>
 * <li><strong>依赖组件:</strong> PostService (内容业务逻辑), JwtUtil (令牌解析)</li>
 * </ul>
 *
 * @author Your Name
 * @version 1.0
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final JwtUtil jwtUtil;

    /**
     * <h2>4.1 文章列表/归档 (List Posts)</h2>
     * <p>
     * 获取已发布的文章列表。通常用于首页展示或归档页面。
     * 返回的数据不包含文章正文内容，仅包含摘要和元数据。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /posts</li>
     * <li><strong>请求方式:</strong> GET</li>
     * <li><strong>权限级别:</strong> <span style="color: green">公开 (Public)</span></li>
     * </ul>
     *
     * @return {@link Result} 包含 {@link PostDtos} 列表
     */
    @GetMapping("/posts")
    public ResponseEntity<Result<List<PostDtos>>> listPosts() {
        List<PostDtos> posts = postService.listAll();
        return ResponseEntity.ok(Result.ok(posts));
    }

    /**
     * <h2>4.2 文章详情 (Get Post Detail)</h2>
     * <p>
     * 根据自定义路径（slug）获取文章的完整内容（包含Markdown/HTML正文）。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /post/{slug}</li>
     * <li><strong>请求方式:</strong> GET</li>
     * <li><strong>权限级别:</strong> <span style="color: green">公开 (Public)</span></li>
     * </ul>
     *
     * @param slug 文章的唯一标识符（URL friendly）
     * @return {@link Result} 包含 {@link PostDetailDto} 文章详情
     */
    @GetMapping("/post/{slug}")
    public ResponseEntity<Result<PostDetailDto>> getPost(@PathVariable String slug) {
        PostDetailDto dto = postService.getBySlug(slug);
        return ResponseEntity.ok(Result.ok(dto));
    }

    /**
     * <h2>4.3 站点统计 (Site Stats)</h2>
     * <p>
     * 获取首页侧边栏所需的统计信息（如文章总数、分类总数、标签总数等）。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /stats</li>
     * <li><strong>请求方式:</strong> GET</li>
     * <li><strong>权限级别:</strong> <span style="color: green">公开 (Public)</span></li>
     * </ul>
     *
     * @return {@link Result} 包含 {@link SiteStatsDto} 统计数据
     */
    @GetMapping("/stats")
    public ResponseEntity<Result<SiteStatsDto>> siteStats() {
        SiteStatsDto dto = postService.stats();
        return ResponseEntity.ok(Result.ok(dto));
    }

    /**
     * <h2>4.4 新建或编辑文章 (Create or Update Post)</h2>
     * <p>
     * 管理员发布新文章或更新现有文章。
     * 若 DTO 中包含 ID 则为更新，否则为新建。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /admin/post</li>
     * <li><strong>请求方式:</strong> POST</li>
     * <li><strong>权限级别:</strong> <span style="color: red">需管理员权限 (ROLE_ADMIN)</span></li>
     * </ul>
     *
     * @param dto {@link PostDtor} 文章表单数据
     * @param authHeader HTTP 请求头中的 Authorization (用于备用 Token 解析)
     * @return {@link Result} 操作成功提示
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/post")
    public ResponseEntity<Result<Void>> createOrUpdate(
            @Valid @RequestBody PostDtor dto,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        // ① 优先通过 Spring Security 上下文获取 userId (标准方式)
        Long userId = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                userId = (Long) principal;
            }
        }

        // ② 若 SecurityContext 没有（极少情况，如 Filter 异常），尝试手动解析 JWT
        if (userId == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.parseToken(token);
                userId = Long.valueOf(claims.getSubject());
            } catch (Exception e) {
                // Token 解析失败，保持 userId 为 null
            }
        }

        // 安全校验
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Result.fail(401, "未能获取登录用户信息，请重新登录"));
        }

        postService.createOrUpdate(dto, userId);
        return ResponseEntity.ok(Result.ok(null));
    }

    /**
     * <h2>4.5 删除文章 (Delete Post)</h2>
     * <p>
     * 根据 ID 删除文章。通常执行软删除（标记删除）以保留数据记录。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /admin/post/{id}</li>
     * <li><strong>请求方式:</strong> DELETE</li>
     * <li><strong>权限级别:</strong> <span style="color: red">需管理员权限 (ROLE_ADMIN)</span></li>
     * </ul>
     *
     * @param id 文章 ID
     * @return {@link Result} 操作成功提示
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/post/{id}")
    public ResponseEntity<Result<Void>> deletePost(@PathVariable Long id) {
        postService.delete(id);
        return ResponseEntity.ok(Result.ok(null));
    }
}