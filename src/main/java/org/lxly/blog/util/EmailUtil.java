package org.lxly.blog.util;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * 采用 static 方法的实现（内部使用一次性保存的 static JavaMailSender）。
 * 仍然是一个 Spring Bean，Spring 启动时会把 mailSender 注入到 static 变量里。
 */
@Component
@Slf4j
public class EmailUtil {

    /** 真实的 JavaMailSender，由 Spring 注入后放入 static 变量 */
    private static JavaMailSender staticMailSender;

    /** 让 Spring 在 Bean 初始化时把 mailSender 赋给 static 变量 */
    @Autowired
    public EmailUtil(@Lazy JavaMailSender mailSender) {
        EmailUtil.staticMailSender = mailSender;
    }

    /**
     * 静态入口：发送 HTML 邮件。
     *
     * @param from        发件人
     * @param to          收件人
     * @param subject     邮件标题
     * @param htmlContent HTML 正文
     */
    public static void sendHtmlMail(String from, String to, String subject, String htmlContent) {
        if (staticMailSender == null) {
            log.error("JavaMailSender 还未初始化，无法发送邮件");
            throw new IllegalStateException("邮件发送组件未准备好");
        }
        MimeMessage message = staticMailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            staticMailSender.send(message);
        } catch (Exception e) {
            log.error("邮件发送失败", e);
            throw new RuntimeException("邮件发送失败", e);
        }
    }
}
