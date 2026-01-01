package org.lxly.blog;

// 在任意 Java 环境（IDE、单元测试、main 方法）运行一次即可
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptDemo {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "123456";                 // 你想测试的明文密码
        String hash = encoder.encode(raw);
        System.out.println(hash);
    }
}
