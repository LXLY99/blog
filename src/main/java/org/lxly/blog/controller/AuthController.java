package org.lxly.blog.controller;

import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.*;
import org.lxly.blog.dto.request.*;
import org.lxly.blog.dto.response.*;
import org.lxly.blog.entity.*;
import org.lxly.blog.service.AuthService;
import org.lxly.blog.config.JwtUtil;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/user-login")
    public ResponseEntity<Result<String>> login(@Valid @RequestBody LoginRequest req) {
        User user = authService.login(req);
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), Boolean.TRUE.equals(user.getIsAdmin()));
        return ResponseEntity.ok(Result.ok(token));
    }

    @PostMapping("/user-register")
    public ResponseEntity<Result<Void>> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok(Result.ok(null));
    }

    @PostMapping("/send-verification-code")
    public ResponseEntity<Result<Void>> sendCode(@Valid @RequestBody VerifyCodeRequest req) {
        authService.sendVerificationCode(req.getEmail(), req.getType());
        return ResponseEntity.ok(Result.ok(null));
    }

    @GetMapping("/user-info")
    public ResponseEntity<Result<UserInfoDto>> userInfo(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
        Long userId = extractUserId(authHeader);
        UserInfoDto dto = authService.getCurrentUserInfo(userId);
        return ResponseEntity.ok(Result.ok(dto));
    }

    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout() {
        // 如需实现 JWT 黑名单，可在此写入 Redis
        return ResponseEntity.ok(Result.ok(null));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Result<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.verifyCode(req.getEmail(), req.getCode(), "password-reset");
        authService.changePassword(req.getEmail(), req.getNewPassword());
        return ResponseEntity.ok(Result.ok(null));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Result<Void>> changePassword(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody ChangePasswordRequest req) {
        Long userId = extractUserId(authHeader);
        // 这里通过 userId 再次确认邮箱属于当前用户
        UserInfoDto info = authService.getCurrentUserInfo(userId);
        if (!info.getEmail().equalsIgnoreCase(req.getEmail())) {
            throw new IllegalArgumentException("邮箱与登录用户不匹配");
        }
        authService.verifyCode(req.getEmail(), req.getCode(), "password-reset");
        authService.changePassword(req.getEmail(), req.getNewPassword());
        return ResponseEntity.ok(Result.ok(null));
    }

    @PutMapping("/user-profile")
    public ResponseEntity<Result<Void>> updateProfile(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader,
            @Valid @RequestBody UpdateProfileRequest req) {
        Long userId = extractUserId(authHeader);
        authService.updateProfile(userId, req);
        return ResponseEntity.ok(Result.ok(null));
    }

    /** 解析 token 中的 userId */
    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("缺少 Token");
        }
        Claims claims = jwtUtil.parseToken(authHeader.substring(7));
        return Long.valueOf(claims.getSubject());
    }
}
