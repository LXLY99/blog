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

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    // ✅ 放行路径：/api/user-login
    @PostMapping("/user-login")
    public ResponseEntity<Result<String>> login(@Valid @RequestBody LoginRequest req) {
        User user = authService.login(req);
        String token = jwtUtil.generateToken(
                user.getId(),
                user.getUsername(),
                Boolean.TRUE.equals(user.getIsAdmin())
        );
        return ResponseEntity.ok(Result.ok(token));
    }

    // ✅ 放行路径：/api/user-register
    @PostMapping("/user-register")
    public ResponseEntity<Result<Void>> register(@Valid @RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.ok(Result.ok(null));
    }

    // ✅ 放行路径：/api/send-verification-code
    @PostMapping("/send-verification-code")
    public ResponseEntity<Result<Void>> sendCode(@Valid @RequestBody VerifyCodeRequest req) {
        authService.sendVerificationCode(req.getEmail(), req.getType());
        return ResponseEntity.ok(Result.ok(null));
    }

    // ✅ 需要登录：从 Authentication 里取 userId（JwtAuthFilter 放进去的）
    @GetMapping("/user-info")
    public ResponseEntity<Result<UserInfoDto>> userInfo(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        UserInfoDto dto = authService.getCurrentUserInfo(userId);
        return ResponseEntity.ok(Result.ok(dto));
    }

    // ✅ 需要登录
    @PostMapping("/logout")
    public ResponseEntity<Result<Void>> logout() {
        return ResponseEntity.ok(Result.ok(null));
    }

    // ✅ 放行路径：/api/reset-password
    @PostMapping("/reset-password")
    public ResponseEntity<Result<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        authService.verifyCode(req.getEmail(), req.getCode(), "password-reset");
        authService.changePassword(req.getEmail(), req.getNewPassword());
        return ResponseEntity.ok(Result.ok(null));
    }

    // ✅ 需要登录
    @PostMapping("/change-password")
    public ResponseEntity<Result<Void>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest req) {

        Long userId = (Long) authentication.getPrincipal();

        UserInfoDto info = authService.getCurrentUserInfo(userId);
        if (!info.getEmail().equalsIgnoreCase(req.getEmail())) {
            throw new IllegalArgumentException("邮箱与登录用户不匹配");
        }

        authService.verifyCode(req.getEmail(), req.getCode(), "password-reset");
        authService.changePassword(req.getEmail(), req.getNewPassword());
        return ResponseEntity.ok(Result.ok(null));
    }

    // ✅ 需要登录
    @PutMapping("/user-profile")
    public ResponseEntity<Result<Void>> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest req) {

        Long userId = (Long) authentication.getPrincipal();
        authService.updateProfile(userId, req);
        return ResponseEntity.ok(Result.ok(null));
    }
}
