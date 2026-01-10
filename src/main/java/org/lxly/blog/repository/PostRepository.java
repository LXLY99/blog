package org.lxly.blog.repository;

import org.lxly.blog.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    // 1. Find single post (Public view - must not be soft deleted)
    Optional<Post> findBySlugAndDeletedFalse(String slug);

    // 2. Find single post (Internal use - can find soft deleted if needed, or for checking duplicates)
    Optional<Post> findBySlug(String slug);

    // 3. Find all public posts for Home Page
    @Query("SELECT p FROM Post p WHERE p.published = true AND p.deleted = false ORDER BY p.createdAt DESC")
    List<Post> findAllByPublishedTrueOrderByCreatedAtDesc();

    // 4. Find public posts by Category
    @Query("SELECT p FROM Post p WHERE p.category = :category AND p.published = true AND p.deleted = false ORDER BY p.createdAt DESC")
    List<Post> findAllByCategoryAndPublishedTrueAndDeletedFalseOrderByCreatedAtDesc(@Param("category") String category);

    // 5. Find public posts by Tag (Using JOIN)
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t = :tag AND p.published = true AND p.deleted = false ORDER BY p.createdAt DESC")
    List<Post> findAllByTagAndDeletedFalse(@Param("tag") String tag);

    // 6. ✅ NEW: Admin - Find ALL posts (Including Drafts, but not deleted)
    // This is used for the "Manage Articles" table in the dashboard
    List<Post> findAllByDeletedFalseOrderByCreatedAtDesc();
}