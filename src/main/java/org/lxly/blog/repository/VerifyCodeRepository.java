package org.lxly.blog.repository;

import org.lxly.blog.entity.VerifyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerifyCodeRepository extends JpaRepository<VerifyCode, Long> {

    /**
     * 查询最新的一条验证码记录（用于校验）
     */
    Optional<VerifyCode> findTopByEmailAndTypeOrderByExpireAtDesc(String email, String type);

    /**
     * ✅ 新增方法：统计指定时间之后该邮箱发送了多少次验证码
     * 用于 1 分钟防刷校验
     * Spring Data JPA 会自动将其转换为 SQL:
     * SELECT COUNT(*) FROM verify_code WHERE email = ? AND type = ? AND created_at > ?
     */
    long countByEmailAndTypeAndCreatedAtAfter(String email, String type, LocalDateTime time);
}