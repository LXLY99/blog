package org.lxly.blog;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class EmailUtilTest {

    @Autowired
    private JavaMailSender mailSender;

    @Test
    void send_direct_test() {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom("2945706262@qq.com");
        msg.setTo("256641681@qq.com");
        msg.setSubject("Direct MailSender test");
        msg.setText("Hello MailHog! " + System.currentTimeMillis());
        mailSender.send(msg);
    }
}