package org.lxly.blog.controller;

import lombok.*;
import org.lxly.blog.service.UploadService;
import org.lxly.blog.dto.response.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.*;

/**
 * <h1>文件上传与资源管理模块 (Controller Layer)</h1>
 * <p>
 * 处理系统中的文件上传请求。
 * 支持图片、文档等静态资源的上传，将文件保存至本地存储或对象存储（OSS），并返回可访问的 URL 地址。
 * </p>
 *
 * <ul>
 * <li><strong>基础路径:</strong> /api</li>
 * <li><strong>依赖组件:</strong> UploadService (文件存储业务逻辑)</li>
 * </ul>
 *
 * @author Your Name
 * @version 1.0
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    /**
     * <h2>7.1 上传文件 (Upload File)</h2>
     * <p>
     * 接收前端上传的二进制文件（通常是图片），保存后返回文件的访问路径。
     * 支持 Markdown 编辑器图片上传、用户头像上传等场景。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /upload</li>
     * <li><strong>请求方式:</strong> POST</li>
     * <li><strong>Content-Type:</strong> multipart/form-data</li>
     * <li><strong>权限级别:</strong> <span style="color: red">需认证 (Authenticated)</span> - 防止恶意文件上传</li>
     * </ul>
     *
     * @param file {@link MultipartFile} 前端上传的文件对象
     * @return {@link Result} 包含 {@link UploadResult}，其中封装了文件的访问 URL
     * @throws Exception 文件读写异常或格式不支持异常
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Result<UploadResult>> upload(@RequestPart("file") MultipartFile file) throws Exception {
        String url = uploadService.upload(file);
        return ResponseEntity.ok(Result.ok(new UploadResult(url)));
    }

    /**
     * 上传结果封装类
     */
    @Getter @AllArgsConstructor
    static class UploadResult {
        /**
         * 文件访问的完整 URL 地址
         */
        private String url;
    }
}