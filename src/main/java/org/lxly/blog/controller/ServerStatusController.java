package org.lxly.blog.controller;

import lombok.RequiredArgsConstructor;
import org.lxly.blog.dto.response.Result;
import org.lxly.blog.service.ServerStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * <h1>系统监控与状态模块 (Controller Layer)</h1>
 * <p>
 * 提供服务器运行状态的实时监控接口。
 * 用于前端展示服务器的负载、内存使用率、运行时间等核心指标，帮助管理员了解系统健康状况。
 * </p>
 *
 * <ul>
 * <li><strong>基础路径:</strong> /api</li>
 * <li><strong>依赖组件:</strong> ServerStatusService (系统信息采集)</li>
 * </ul>
 *
 * @author Your Name
 * @version 1.0
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ServerStatusController {

    private final ServerStatusService statusService;

    /**
     * <h2>5.1 获取服务器状态 (Get Server Status)</h2>
     * <p>
     * 获取当前服务器的实时运行指标。
     * 数据通常包含：CPU 使用率、内存占用、JVM 状态、系统运行时间、操作系统信息等。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /server-status</li>
     * <li><strong>请求方式:</strong> GET</li>
     * <li><strong>权限级别:</strong> <span style="color: green">公开 (Public)</span> - 通常展示在页脚或“关于”页面</li>
     * </ul>
     *
     * @return {@link Result} 包含 {@link Map} 类型的键值对状态数据
     */
    @GetMapping("/server-status")
    public ResponseEntity<Result<Map<String, Object>>> status() {
        return ResponseEntity.ok(Result.ok(statusService.getStatus()));
    }
}