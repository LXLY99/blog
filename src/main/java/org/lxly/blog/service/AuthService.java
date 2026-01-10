package org.lxly.blog.service;

import lombok.RequiredArgsConstructor;
import org.lxly.blog.dto.request.LoginRequest;
import org.lxly.blog.dto.request.RegisterRequest;
import org.lxly.blog.dto.request.UpdateProfileRequest;
import org.lxly.blog.dto.response.UserInfoDto;
import org.lxly.blog.entity.User;
import org.lxly.blog.entity.VerifyCode;
import org.lxly.blog.exception.BizException;
import org.lxly.blog.repository.UserRepository;
import org.lxly.blog.repository.VerifyCodeRepository;
import org.lxly.blog.util.EmailUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final VerifyCodeRepository verifyCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailUtil emailUtil;
    private final StringRedisTemplate redisTemplate;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${smms.default-avatar}")
    private String defaultAvatarUrl;

    public User login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BizException("User does not exist"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException("Incorrect password");
        }
        return user;
    }

    @Transactional
    public void register(RegisterRequest req) {
        // 1. Verify Code first
        // verifyCode(req.getEmail(), req.getCode(), VerifyCodeType.REGISTER.getValue());

        // 2. Check if Email exists
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new BizException("Email already registered");
        }

        // 3. ✅ Generate Fixed Pattern Username using Redis
        // "blog:user:sequence" will atomically increment: 1, 2, 3...
        Long seq = redisTemplate.opsForValue().increment("blog:user:sequence");
        String autoUsername = "lxly_" + seq;

        // 4. Create User
        User user = User.builder()
                .email(req.getEmail())
                .username(autoUsername) // ✅ Fixed Username
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname("User_" + seq) // Default nickname (can be changed later)
                .avatar(defaultAvatarUrl)
                .isAdmin(false)
                .createdAt(LocalDateTime.now())
                .build();

        userRepository.save(user);
    }

    public void sendVerificationCode(String email, String typeStr) {
        // ✅ NEW: Check if user exists for password reset
        // This prevents sending codes to unregistered emails
        if ("password-reset".equals(typeStr)) {
            if (userRepository.findByEmail(email).isEmpty()) {
                throw new BizException("This email is not registered");
            }
        }

        // 1. Check Rate Limit (1 minute)
        LocalDateTime oneMinuteAgo = LocalDateTime.now().minusMinutes(1);
        long count = verifyCodeRepository.countByEmailAndTypeAndCreatedAtAfter(email, typeStr, oneMinuteAgo);
        if (count > 0) {
            throw new BizException("Too many requests, please try again later");
        }

        // 2. Generate Code
        String code = String.valueOf(new Random().nextInt(900000) + 100000);

        // 3. Save to DB
        VerifyCode vc = VerifyCode.builder()
                .email(email)
                .code(code)
                .type(typeStr)
                .expireAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();
        verifyCodeRepository.save(vc);

        // 4. Send Email
        String subject = "GL-Blog Verification";

        String htmlContent = """
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2 style="color: #000;">GL-Blog Verification</h2>
                    <p>Dear User,</p>
                    <p>Your verification code is: <strong style="font-size: 18px; color: #0066cc;">%s</strong></p>
                    <p>This code is valid for 10 minutes.</p>
                    <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 12px; color: #999;">If you did not request this, please ignore this email.</p>
                </div>
                """.formatted(code);

        emailUtil.sendHtmlMail(fromEmail, email, subject, htmlContent);
    }

    @Transactional
    public void verifyCode(String email, String code, String typeStr) {
        VerifyCode vc = verifyCodeRepository.findTopByEmailAndTypeOrderByExpireAtDesc(email, typeStr)
                .orElseThrow(() -> new BizException("Verification code does not exist or expired"));

        if (vc.getUsed()) {
            throw new BizException("Verification code has been used");
        }

        if (LocalDateTime.now().isAfter(vc.getExpireAt())) {
            throw new BizException("Verification code has expired");
        }

        if (!vc.getCode().equals(code)) {
            throw new BizException("Incorrect verification code");
        }

        // Mark as used
        vc.setUsed(true);
        verifyCodeRepository.save(vc);
    }

    public UserInfoDto getCurrentUserInfo(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException("User not found"));

        return UserInfoDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .isAdmin(user.getIsAdmin())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BizException("User not found"));

        if (req.getNickname() != null) user.setNickname(req.getNickname());
        if (req.getAvatar() != null) user.setAvatar(req.getAvatar());

        // If username changes, check uniqueness
        if (req.getUsername() != null && !req.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(req.getUsername()).isPresent()) {
                throw new BizException("Username already exists");
            }
            user.setUsername(req.getUsername());
        }

        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BizException("User does not exist"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void resetPassword(String email, String code, String newPassword) {
        // 1. Verify the code first
        verifyCode(email, code, "password-reset");

        // 2. Find the user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BizException("User does not exist"));

        // 3. 🔥 NEW LOGIC: Check if the new password is the same as the old one
        // We use passwordEncoder.matches(raw, hashed) to compare
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new BizException("New password cannot be the same as the old password");
        }

        // 4. Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}