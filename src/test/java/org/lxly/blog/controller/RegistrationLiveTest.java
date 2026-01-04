package org.lxly.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.lxly.blog.dto.request.RegisterRequest;
import org.lxly.blog.dto.request.VerifyCodeRequest;
import org.lxly.blog.entity.VerifyCode;
import org.lxly.blog.enums.VerifyCodeType;
import org.lxly.blog.repository.UserRepository;
import org.lxly.blog.repository.VerifyCodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // 测试结束后自动回滚数据库，保持环境干净
public class RegistrationLiveTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private VerifyCodeRepository codeRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ObjectMapper objectMapper;

    /**
     * 测试点 1：真实发送邮件接口
     * 运行此测试后，请检查您的真实邮箱，应该能收到验证码。
     */
    @Test
    public void testSendVerificationCode_RealEmail() throws Exception {
        // ⚠️ 请替换为您真实的接收邮箱，否则收不到邮件
        // String myEmail = "256641681@qq.com";
        String myEmail = "2416915166@qq.com";

        // 1. 构造请求
        // 注意：VerifyCodeRequest 需要有 email 和 type 字段
        // 这里手动构建 JSON 字符串以避免 DTO 字段不匹配问题，或者您可以使用 VerifyCodeRequest 对象
        String jsonRequest = objectMapper.createObjectNode()
                .put("email", myEmail)
                .put("type", "register")
                .toString();

        System.out.println(">>> 正在调用发送接口，目标邮箱: " + myEmail);

        // 2. 调用接口 /api/send-verification-code
        mockMvc.perform(post("/api/send-verification-code")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk()) // 期望 HTTP 200
                .andExpect(jsonPath("$.code").value(0)); // 期望业务码 0 (成功)

        System.out.println(">>> 接口调用成功！正在等待异步邮件发送 (5秒)...");

        // 3. ⏳ 关键步骤：因为发送邮件是 @Async 异步的，测试主线程必须等待
        // 否则测试结束 JVM 关闭，邮件还没发出去线程就被杀掉了
        Thread.sleep(5000);

        System.out.println(">>> 等待结束，请检查您的邮箱。");

        // 4. 验证数据库里是否生成了记录
        boolean exists = codeRepo.countByEmailAndTypeAndCreatedAtAfter(
                myEmail, "register", LocalDateTime.now().minusMinutes(1)) > 0;
        assertTrue(exists, "数据库中应该生成了验证码记录");
    }

    /**
     * 测试点 2：完整的注册流程 (验证码逻辑校验)
     * 这个测试主要验证"填对了验证码能注册成功"，不负责发真实邮件
     */
    @Test
    public void testFullRegistrationFlow() throws Exception {
        String email = "integration-test@example.com";
        String code = "123456";

        // 1. 预置数据：直接往数据库插入一个有效的验证码
        // 这样测试注册时就不需要真的去收邮件
        VerifyCode vc = VerifyCode.builder()
                .email(email)
                .code(code)
                .type(VerifyCodeType.REGISTER.getValue())
                .expireAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();
        codeRepo.save(vc);

        // 2. 准备注册请求
        RegisterRequest req = new RegisterRequest();
        req.setEmail(email);
        req.setUsername("integrationUser");
        req.setPassword("SecurePass123!");
        req.setCode(code); // 必须与数据库里预置的一致

        // 3. 发起注册 POST 请求
        mockMvc.perform(post("/api/user-register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0));

        // 4. 验证数据库副作用
        // 验证用户是否创建
        assertTrue(userRepo.findByEmail(email).isPresent(), "用户应该已经创建成功");

        // 验证验证码是否被标记为已使用
        VerifyCode usedCode = codeRepo.findAll().stream()
                .filter(c -> c.getEmail().equals(email))
                .findFirst()
                .orElseThrow();
        assertTrue(usedCode.getUsed(), "验证码应该被标记为已使用");
    }
}