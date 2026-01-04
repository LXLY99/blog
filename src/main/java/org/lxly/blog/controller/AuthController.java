package org.lxly.blog.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lxly.blog.config.JwtUtil;
import org.lxly.blog.dto.request.*;
import org.lxly.blog.dto.response.Result;
import org.lxly.blog.dto.response.UserInfoDto;
import org.lxly.blog.entity.User;
import org.lxly.blog.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * <h1>认证与用户管理模块 (Controller Layer)</h1>
 * <p>
 * 博客系统的核心认证控制器，主要负责处理用户的注册、登录、身份验证、密码管理以及个人信息维护。
 * 该模块实现了前后端分离架构下的 JWT（JSON Web Token）认证机制。
 * </p>
 *
 * <ul>
 * <li><strong>基础路径:</strong> /api</li>
 * <li><strong>依赖组件:</strong> AuthService (业务逻辑), JwtUtil (令牌工具)</li>
 * </ul>
 *
 * @author Your Name
 * @version 1.0
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * <h2>3.1 用户登录 (User Login)</h2>
     * <p>
     * 验证用户凭证（用户名/邮箱 + 密码），验证通过后签发 JWT 令牌。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /user-login</li>
     * <li><strong>请求方式:</strong> POST</li>
     * <li><strong>权限级别:</strong> <span style="color: green">公开 (Public)</span> - 在 SecurityConfig 中放行</li>
     * </ul>
     *
     * @param req {@link LoginRequest} 包含用户名/邮箱和密码
     * @return {@link Result} 包含生成的 JWT Token 字符串
     */
    @PostMapping("/user-login")
    public ResponseEntity<Result<String>> login(@Valid @RequestBody LoginRequest req) {
        User user = authService.login(req);
        // 生成包含 userId, username, isAdmin 的 Token
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                Boolean.TRUE.equals(user.getIsAdmin())
        );
        return ResponseEntity.ok(Result.ok(token));
    }

    /**
     * <h2>3.2 用户注册 (User Register)</h2>
     * <p>
     * 处理新用户注册请求，校验验证码并创建账户。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /user-register</li>
     * <li><strong>请求方式:</strong> POST</li>
     * <li><strong>权限级别:</strong> <span style="color: green">公开 (Public)</span> - 在 SecurityConfig 中放行</li>
     * </ul>
     *
     * @param req {@link RegisterRequest} 包含邮箱、验证码、用户名、密码
     * @return {@link Result} 成功返回空数据，失败抛出异常
     */
    @PostMapping("/user-register")
    public ResponseEntity<Result<Void>> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok(Result.ok(null));
    }

    /**
     * <h2>3.3 发送验证码 (Send Verification Code)</h2>
     * <p>
     * 向指定邮箱发送 6 位数字验证码（用于注册或重置密码）。包含 1 分钟防刷限制。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /send-verification-code</li>
     * <li><strong>请求方式:</strong> POST</li>
     * <li><strong>权限级别:</strong> <span style="color: green">公开 (Public)</span> - 在 SecurityConfig 中放行</li>
     * </ul>
     *
     * @param req {@link VerifyCodeRequest} 包含目标邮箱和业务类型 (type)
     * @return {@link Result} 成功发送返回 OK
     */
    @PostMapping("/send-verification-code")
    public ResponseEntity<Result<Void>> sendCode(@Valid @RequestBody VerifyCodeRequest req) {
        authService.sendVerificationCode(req.getEmail(), req.getType());
        return ResponseEntity.ok(Result.ok(null));
    }

    /**
     * <h2>3.4 获取用户信息 (Get User Info)</h2>
     * <p>
     * 获取当前登录用户的详细个人资料。
     * UserID 不是通过参数传递，而是从 Spring Security 上下文 (Authentication) 中安全获取。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /user-info</li>
     * <li><strong>请求方式:</strong> GET</li>
     * <li><strong>权限级别:</strong> <span style="color: red">需认证 (Authenticated)</span></li>
     * </ul>
     *
     * @param authentication 由 JwtAuthFilter 注入的认证对象，Principal 为 userId
     * @return {@link Result} 包含 {@link UserInfoDto} 用户详情
     */
    @GetMapping("/user-info")
    public ResponseEntity<Result<UserInfoDto>> userInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserInfoDto dto = authService.getCurrentUserInfo(userId);
        return ResponseEntity.ok(Result.ok(dto));
    }

    /**
     * <h2>3.5 退出登录 (Logout)</h2>
     * <p>
     * 执行登出操作。由于采用 JWT 无状态认证，服务端主要作记录，客户端需自行销毁 Token。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /logout</li>
     * <li><strong>请求方式:</strong> POST</li>
     * <li><strong>权限级别:</strong> <span style="color: red">需认证 (Authenticated)</span></li>
     * </ul>
     *
     * @return {@link Result} 成功提示
     */
    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout() {
        return ResponseEntity.ok(Result.ok(null));
    }

    /**
     * <h2>3.6 重置密码 (Reset Password)</h2>
     * <p>
     * 用户忘记密码时，通过邮箱验证码重置密码。无需登录状态。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /reset-password</li>
     * <li><strong>请求方式:</strong> POST</li>
     * <li><strong>权限级别:</strong> <span style="color: green">公开 (Public)</span> - 在 SecurityConfig 中放行</li>
     * </ul>
     *
     * @param req {@link ResetPasswordRequest} 包含邮箱、验证码、新密码
     * @return {@link Result} 修改成功提示
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Result<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        // 校验验证码类型固定为 "password-reset"
        authService.verifyCode(req.getEmail(), req.getCode(), "password-reset");
        authService.changePassword(req.getEmail(), req.getNewPassword());
        return ResponseEntity.ok(Result.ok(null));
    }

    /**
     * <h2>3.7 修改密码 (Change Password)</h2>
     * <p>
     * 已登录用户修改自己的密码。
     * 为了安全，强制校验当前登录用户的邮箱与请求参数中的邮箱是否一致，防止越权修改他人密码。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /change-password</li>
     * <li><strong>请求方式:</strong> POST</li>
     * <li><strong>权限级别:</strong> <span style="color: red">需认证 (Authenticated)</span></li>
     * </ul>
     *
     * @param authentication 当前登录用户凭证
     * @param req {@link ChangePasswordRequest} 包含确认邮箱、验证码、新密码
     * @return {@link Result} 修改成功提示
     * @throws IllegalArgumentException 如果请求邮箱与登录用户不匹配
     */
    @PostMapping("/change-password")
    public ResponseEntity<Result<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest req) {

        Long userId = (Long) authentication.getPrincipal();

        // 安全检查：防止越权操作
        UserInfoDto info = authService.getCurrentUserInfo(userId);
        if (!info.getEmail().equalsIgnoreCase(req.getEmail())) {
            throw new IllegalArgumentException("邮箱与登录用户不匹配");
        }

        authService.verifyCode(req.getEmail(), req.getCode(), "password-reset");
        authService.changePassword(req.getEmail(), req.getNewPassword());
        return ResponseEntity.ok(Result.ok(null));
    }

    /**
     * <h2>3.8 更新个人资料 (Update Profile)</h2>
     * <p>
     * 更新当前用户的非敏感信息（如昵称、头像）。
     * </p>
     *
     * <ul>
     * <li><strong>接口地址:</strong> /user-profile</li>
     * <li><strong>请求方式:</strong> PUT</li>
     * <li><strong>权限级别:</strong> <span style="color: red">需认证 (Authenticated)</span></li>
     * </ul>
     *
     * @param authentication 当前登录用户凭证
     * @param req {@link UpdateProfileRequest} 包含新昵称、新头像 URL
     * @return {@link Result} 更新成功提示
     */
    @PutMapping("/user-profile")
    public ResponseEntity<Result<Void>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest req) {

        Long userId = (Long) authentication.getPrincipal();
        authService.updateProfile(userId, req);
        return ResponseEntity.ok(Result.ok(null));
    }
}