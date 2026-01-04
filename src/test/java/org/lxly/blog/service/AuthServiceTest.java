package org.lxly.blog.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.lxly.blog.dto.request.RegisterRequest;
import org.lxly.blog.entity.User;
import org.lxly.blog.entity.VerifyCode;
import org.lxly.blog.enums.VerifyCodeType;
import org.lxly.blog.exception.BizException;
import org.lxly.blog.repository.SettingsRepository;
import org.lxly.blog.repository.UserRepository;
import org.lxly.blog.repository.VerifyCodeRepository;
import org.lxly.blog.util.EmailUtil;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepo;
    @Mock private VerifyCodeRepository codeRepo;
    @Mock private SettingsRepository settingsRepo;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailUtil emailUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void sendVerificationCode_Success() {
        // 1. 准备数据
        String email = "256641681@qq.com";
        String type = "register";

        // 2. 模拟数据库行为 (Count = 0)
        when(codeRepo.countByEmailAndTypeAndCreatedAtAfter(
                eq(email),
                eq(type),
                any(LocalDateTime.class))
        ).thenReturn(0L);

        // 3. 执行方法
        authService.sendVerificationCode(email, type);

        // 4. 🔥 修正验证逻辑：匹配实际的英文主题
        verify(emailUtil, times(1)).sendHtmlMail(
                any(),
                eq(email),
                contains("Verification"), // 修正：匹配实际的英文主题
                contains("GL-Blog")       // 修正：匹配实际的 HTML 内容片段
        );

        // 5. 验证数据库保存
        verify(codeRepo, times(1)).save(any(VerifyCode.class));
    }

    @Test
    void sendVerificationCode_RateLimit() {
        String email = "test@example.com";
        String type = "register";

        // 模拟：1分钟内已经有一条记录了
        when(codeRepo.countByEmailAndTypeAndCreatedAtAfter(any(), any(), any()))
                .thenReturn(1L);

        // 断言：应该抛出业务异常
        BizException ex = assertThrows(BizException.class, () ->
                authService.sendVerificationCode(email, type)
        );

        // 🔥 修正断言：匹配实际的英文错误提示
        assertEquals("Too many requests, please try again later", ex.getMessage());

        // 验证：绝对不能发送邮件
        verify(emailUtil, never()).sendHtmlMail(any(), any(), any(), any());
    }

    @Test
    void register_Success() {
        // 1. 准备请求
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@example.com");
        req.setUsername("newuser");
        req.setPassword("password123");
        req.setCode("888888");

        VerifyCode validCode = VerifyCode.builder()
                .code("888888")
                .email("test@example.com")
                .type(VerifyCodeType.REGISTER.getValue())
                .expireAt(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .build();

        when(codeRepo.findTopByEmailAndTypeOrderByExpireAtDesc(
                req.getEmail(), VerifyCodeType.REGISTER.getValue()))
                .thenReturn(Optional.of(validCode));

        when(userRepo.findByEmail(any())).thenReturn(Optional.empty());
        when(userRepo.findByUsername(any())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encoded_pass");

        // 3. 执行
        authService.register(req);

        // 4. 验证
        verify(userRepo, times(1)).save(any(User.class));
        assertTrue(validCode.getUsed());
    }
}