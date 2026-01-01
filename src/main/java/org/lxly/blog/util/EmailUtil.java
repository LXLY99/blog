package org.lxly.blog.util;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailUtil {

    private final JavaMailSender mailSender;

    /**
     * Asynchronously sends an HTML email.
     * Uses the standard Spring JavaMailSender configured in application.yml.
     */
    @Async
    public void sendHtmlMail(String from, String to, String subject, String htmlContent) {
        log.info("Attempting to send email to: {}", to);
        try {
            MimeMessage message = mailSender.createMimeMessage();
            // 'true' indicates this is a multipart message (supports HTML/Attachments)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = interpret as HTML

            mailSender.send(message);
            log.info("✅ Email successfully sent to: {}", to);
        } catch (Exception e) {
            log.error("❌ Failed to send email to: {}", to, e);
            // In a real async scenario, you might want to send this to a dead-letter queue
        }
    }
}