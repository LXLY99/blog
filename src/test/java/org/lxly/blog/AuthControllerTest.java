package org.lxly.blog;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.lxly.blog.dto.request.LoginRequest;
import org.lxly.blog.dto.response.Result;
import org.lxly.blog.entity.User;
import org.lxly.blog.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate rest;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private org.springframework.mail.javamail.JavaMailSender mailSender;

    private static final String EXISTING_EMAIL = "test@example.com";
    private static final String RAW_PASSWORD = "123456";

    // username 有唯一约束，建议固定一个值
    private static final String EXISTING_USERNAME = "testuser";

    @BeforeEach
    void insertTestUser() {
        // ✅ 先清理：email + username 都清，避免唯一约束
        int del1 = userRepository.deleteByEmail(EXISTING_EMAIL);
        int del2 = userRepository.deleteByUsername(EXISTING_USERNAME);

        // ✅ 保险：把删除立刻 flush 到 DB（避免后面插入时仍然看到旧数据）
        userRepository.flush();

        User user = new User();
        user.setEmail(EXISTING_EMAIL);
        user.setUsername(EXISTING_USERNAME); // ⚠️ 你表里 username NOT NULL + UNIQUE，必须填
        user.setPassword(passwordEncoder.encode(RAW_PASSWORD));
        user.setNickname("TestUser");
        user.setAvatar("https://dummy.avatar/img.png");
        user.setIsAdmin(false);

        userRepository.saveAndFlush(user);

        log.info("✅ 测试用户已写入 DB | delEmailRows={}, delUsernameRows={}, email={}, username={}",
                del1, del2, EXISTING_EMAIL, EXISTING_USERNAME);
    }

    @Test
    @DisplayName("✅ 正确的邮箱+密码 → 登录成功")
    void login_success_when_user_exists() {
        String url = "http://localhost:" + port + "/api/user-login";

        LoginRequest req = new LoginRequest();
        req.setEmail(EXISTING_EMAIL);
        req.setPassword(RAW_PASSWORD);

        ResponseEntity<Result> resp = rest.postForEntity(url, req, Result.class);

        log.info("🔎 登录成功请求返回 → status={}, body={}", resp.getStatusCode(), resp.getBody());

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getCode()).isEqualTo(0);
    }

    @Test
    @DisplayName("❌ 不存在的邮箱 → 登录失败 401")
    void login_fail_when_user_not_exist() {
        String url = "http://localhost:" + port + "/api/user-login";

        LoginRequest req = new LoginRequest();
        req.setEmail("noone_" + System.currentTimeMillis() + "@example.com");
        req.setPassword("123456");

        ResponseEntity<Result> resp = rest.postForEntity(url, req, Result.class);

        log.info("🔎 登录失败请求返回 → status={}, body={}", resp.getStatusCode(), resp.getBody());

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(resp.getBody()).isNotNull();
        assertThat(resp.getBody().getCode()).isGreaterThan(0);
    }
}
