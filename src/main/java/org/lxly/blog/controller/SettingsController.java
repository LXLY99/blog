package org.lxly.blog.controller;

import lombok.RequiredArgsConstructor;
import org.lxly.blog.dto.response.Result;
import org.lxly.blog.dto.response.SettingsDto;
import org.lxly.blog.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * <h1>系统配置与全局设置模块 (Controller Layer)</h1>
 * <p>
 * 负责博客站点的全局配置管理。
 * 包括站点名称、ICP 备案号、页脚信息、以及"关于我"页面的内容维护。
 * 这些配置通常在应用启动或前端页面加载时获取，用于全局渲染。
 * </p>
 *
 * <ul>
 * <li><strong>基础路径:</strong> /api</li>
 * <li><strong>依赖组件:</strong> SettingsService (配置业务逻辑)</li>
 * </ul>
 *
 * @author Your Name
 * @version 1.0
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SettingsController {

    private final SettingsService settingsService;

    /**
     * <h2>6.1 获取全局设置 (Get Site Settings)</h2>
     * <p>
     * 获取博客的公共配置信息。
     * 前端通常在初始化时调用此接口，用于渲染导航栏标题、页脚版权、ICP 备案号等全局元素。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /settings</li>
     * <li><strong>请求方式:</strong> GET</li>
     * <li><strong>权限级别:</strong> <span style="color: green">公开 (Public)</span></li>
     * </ul>
     *
     * @return {@link Result} 包含 {@link SettingsDto} 全局配置对象
     */
    @GetMapping("/settings")
    public ResponseEntity<Result<SettingsDto>> getSettings() {
        return ResponseEntity.ok(Result.ok(settingsService.getSettings()));
    }

    /**
     * <h2>6.2 初始化或更新设置 (Init or Update Settings)</h2>
     * <p>
     * 用于系统初次安装时的初始化，或后续管理员修改站点配置。
     * 支持更新站点标题、描述、公告栏内容等。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /setup</li>
     * <li><strong>请求方式:</strong> POST</li>
     * <li><strong>权限级别:</strong> <span style="color: orange">公开/需管理员</span> (视业务阶段而定，通常需鉴权)</li>
     * </ul>
     *
     * @param dto {@link SettingsDto} 新的配置信息
     * @return {@link Result} 返回更新后的配置信息
     */
    @PostMapping("/setup")
    public ResponseEntity<Result<SettingsDto>> setup(@RequestBody SettingsDto dto) {
        SettingsDto saved = settingsService.initOrUpdate(dto);
        return ResponseEntity.ok(Result.ok(saved));
    }

    /**
     * <h2>6.3 获取关于页面信息 (Get About Info)</h2>
     * <p>
     * 获取专门用于 "关于我" (About) 页面展示的内容。
     * 实际上是返回配置中的 notice (公告/简介) 字段，通常已在服务端渲染为 HTML。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /about</li>
     * <li><strong>请求方式:</strong> GET</li>
     * <li><strong>权限级别:</strong> <span style="color: green">公开 (Public)</span></li>
     * </ul>
     *
     * @return {@link Result} 包含仅填充了 about 内容的 {@link SettingsDto}
     */
    @GetMapping("/about")
    public ResponseEntity<Result<SettingsDto>> getAbout() {
        // 这里实际上返回 Settings 中的 notice 字段（已渲染为 HTML）
        SettingsDto dto = settingsService.getAbout();
        return ResponseEntity.ok(Result.ok(dto));
    }
}