package org.lxly.blog.util;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailUtilLiveTest {

    @Autowired
    private EmailUtil emailUtil;

    // Reads the username directly from your application.yml to ensure consistency
    @Value("${spring.mail.username}")
    private String senderEmail;

    @Test
    void testSendVerificationCodeFormat() throws InterruptedException {
        // 1. Prepare Test Data
        // Replace this with the target email you want to test receiving
        String to = "256641681@qq.com";
        String subject = "GL-Blog Registration Verification";
        String code = "884219"; // Example code

        // 2. HTML Content (Matching your AuthService format)
        String htmlContent = """
                <div style="font-family: Arial, sans-serif; padding: 20px; color: #333;">
                    <h2 style="color: #000;">Welcome to GL-Blog!</h2>
                    <p>Dear User,</p>
                    <p>Your verification code is: <strong style="font-size: 18px; color: #0066cc;">%s</strong></p>
                    <p>This code is valid for 10 minutes.</p>
                    <hr style="border: 0; border-top: 1px solid #eee; margin: 20px 0;">
                    <p style="font-size: 12px; color: #999;">If you did not request this, please ignore this email.</p>
                </div>
                """.formatted(code);

        // 3. Send Email
        System.out.println("Sending test email from: " + senderEmail + " to: " + to);
        emailUtil.sendHtmlMail(senderEmail, to, subject, htmlContent);

        // 4. Wait a bit because the method is @Async
        // Without this, the test might finish before the background thread sends the mail
        System.out.println("Waiting for async delivery...");
        Thread.sleep(5000);
    }
}