package org.lxly.blog.repository;

import org.lxly.blog.entity.VerifyCode;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface VerifyCodeRepository extends JpaRepository<VerifyCode, Long> {
    Optional<VerifyCode> findTopByEmailAndTypeOrderByExpireAtDesc(String email, String type);
}
