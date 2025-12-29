package org.lxly.blog;

import org.junit.jupiter.api.Test;
import org.lxly.blog.dto.request.LoginRequest;
import org.lxly.blog.dto.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;   // ★ 必须
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;   // ★ 必须

import static org.assertj.core.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")            // 让 Spring 用 application-test.yml (H2) 启动
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;      // Spring 已经把随机端口注入进去

    // -------------------------------------------------
    // 防止 EmailUtil 的 staticMailSender 真正去连接 SMTP
    // -------------------------------------------------
    @MockBean
    private org.springframework.mail.javamail.JavaMailSender mailSender;
    // -------------------------------------------------

    @Test
    void login_fail_when_user_not_exist() {
        String url = "http://localhost:" + port + "/api/user-login";

        LoginRequest req = new LoginRequest();
        req.setEmail("noone@example.com");
        req.setPassword("123456");

        ResponseEntity<Result> resp = rest.postForEntity(url, req, Result.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody().getCode()).isGreaterThan(0);
    }
}
