package org.lxly.blog.repository;

import org.lxly.blog.entity.Post;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findBySlugAndDeletedFalse(String slug);
    Optional<Post> findBySlug(String slug); // 只在内部使用（软删时）

    @Query("SELECT p FROM Post p WHERE p.published = true AND p.deleted = false ORDER BY p.createdAt DESC")
    List<Post> findAllByPublishedTrueOrderByCreatedAtDesc();

    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.published = true AND p.deleted = false ORDER BY p.createdAt DESC")
    List<Post> findAllByCategoryAndPublishedTrueAndDeletedFalseOrderByCreatedAtDesc(String category);

    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t = :tag AND p.published = true AND p.deleted = false ORDER BY p.createdAt DESC")
    List<Post> findAllByTagAndDeletedFalse(String tag);
}
