package org.lxly.blog.repository;

import org.lxly.blog.entity.User;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // ✅ 必须加：@Modifying + @Transactional（写事务）
    @Modifying
    @Transactional
    @Query("delete from User u where u.email = :email")
    int deleteByEmail(@Param("email") String email);

    @Modifying
    @Transactional
    @Query("delete from User u where u.username = :username")
    int deleteByUsername(@Param("username") String username);
}
