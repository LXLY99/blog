package org.lxly.blog.service;

import lombok.RequiredArgsConstructor;
import org.lxly.blog.dto.request.*;
import org.lxly.blog.dto.response.UserInfoDto;
import org.lxly.blog.entity.User;
import org.lxly.blog.entity.VerifyCode;
import org.lxly.blog.enums.VerifyCodeType;
import org.lxly.blog.exception.BizException;
import org.lxly.blog.repository.SettingsRepository;
import org.lxly.blog.repository.UserRepository;
import org.lxly.blog.repository.VerifyCodeRepository;
import org.lxly.blog.util.EmailUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final VerifyCodeRepository codeRepo;
    private final SettingsRepository settingsRepo;
    private final PasswordEncoder passwordEncoder;

    // ✅ 注入 EmailUtil 实例（取代静态调用）
    private final EmailUtil emailUtil;

    /** 发件人（从配置中读取） */
    @Value("${spring.mail.username}")
    private String mailFrom;

    /** 注册（带验证码校验） */
    @Transactional
    public void register(RegisterRequest req) {
        // 1️⃣ 校验验证码
        VerifyCode vc = codeRepo.findTopByEmailAndTypeOrderByExpireAtDesc(
                        req.getEmail(),
                        VerifyCodeType.REGISTER.getValue())
                .orElseThrow(() -> new BizException(2001, "验证码不存在"));

        if (vc.getUsed() || vc.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BizException(2002, "验证码已失效或已使用");
        }
        if (!vc.getCode().equals(req.getCode())) {
            throw new BizException(2003, "验证码错误");
        }

        // 2️⃣ 检查邮箱 / 用户名唯一性
        if (userRepo.findByEmail(req.getEmail()).isPresent()) {
            throw new BizException(2004, "邮箱已被占用");
        }
        if (userRepo.findByUsername(req.getUsername()).isPresent()) {
            throw new BizException(2005, "用户名已被占用");
        }

        // 3️⃣ 创建用户（密码使用 BCrypt 加密）
        User user = User.builder()
                .email(req.getEmail())
                .username(req.getUsername())
                .password(passwordEncoder.encode(req.getPassword()))
                .nickname(req.getUsername())
                .isAdmin(false)
                .build();
        userRepo.save(user);

        // 4️⃣ 标记验证码已使用
        vc.setUsed(true);
        codeRepo.save(vc);
    }

    /** 登录校验（返回 User 实体，实际 token 由 Controller 生成） */
    public User login(LoginRequest req) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new BizException(3001, "账号不存在"));
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BizException(3002, "密码错误");
        }
        return user;
    }

    /** 发送验证码（注册 / 找回密码） */
    public void sendVerificationCode(String email, String type) {
        // ① 将外部传入的字符串转成枚举（统一使用大写下划线形式）
        VerifyCodeType vt;
        try {
            vt = VerifyCodeType.valueOf(
                    type.toUpperCase().replace('-', '_'));
        } catch (IllegalArgumentException e) {
            throw new BizException(4001, "不支持的验证码类型");
        }

        // ② 随机生成 6 位数字验证码
        String code = String.format("%06d", new Random().nextInt(999_999));

        // ③ 保存验证码记录
        VerifyCode vc = VerifyCode.builder()
                .email(email)
                .code(code)
                .type(vt.getValue())               // 将枚举的 string value 写入 DB
                .expireAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();
        codeRepo.save(vc);

        // ④ 发送邮件（使用注入的实例方法）
        String subject = switch (vt) {
            case REGISTER -> "GL‑Blog 注册验证码";
            case PASSWORD_RESET -> "GL‑Blog 找回密码验证码";
        };
        String html = """
                <p>亲爱的用户，您的验证码是 <strong>%s</strong>，有效期 10 分钟。</p>
                """.formatted(code);

        // ✅ 实例调用
        emailUtil.sendHtmlMail(mailFrom, email, subject, html);
    }

    /** 校验验证码（找回密码、修改邮箱等） */
    public void verifyCode(String email, String code, String type) {
        VerifyCode vc = codeRepo.findTopByEmailAndTypeOrderByExpireAtDesc(email, type)
                .orElseThrow(() -> new BizException(5001, "验证码不存在"));
        if (vc.getUsed() || vc.getExpireAt().isBefore(LocalDateTime.now())) {
            throw new BizException(5002, "验证码已失效或已使用");
        }
        if (!vc.getCode().equals(code)) {
            throw new BizException(5003, "验证码错误");
        }
        vc.setUsed(true);
        codeRepo.save(vc);
    }

    /** 修改密码（需要先通过验证码校验） */
    @Transactional
    public void changePassword(String email, String newPassword) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new BizException(6001, "用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    /** 获取当前登录用户的完整信息（供前端 Profile 页面使用） */
    public UserInfoDto getCurrentUserInfo(Long userId) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new BizException(7001, "用户不存在"));
        return UserInfoDto.from(user);
    }

    /** 更新个人资料（昵称、用户名、头像） */
    @Transactional
    public void updateProfile(Long userId, UpdateProfileRequest req) {
        User user = userRepo.findById(userId)
                .orElseThrow(() -> new BizException(8001, "用户不存在"));

        // 若用户名被修改，需要再次校验唯一性
        if (!user.getUsername().equals(req.getUsername())) {
            if (userRepo.findByUsername(req.getUsername()).isPresent()) {
                throw new BizException(8002, "用户名已被占用");
            }
            user.setUsername(req.getUsername());
        }

        user.setNickname(req.getNickname());
        if (req.getAvatar() != null) {
            user.setAvatar(req.getAvatar());
        }
        userRepo.save(user);
    }
}