package com.example.boardpjt.model.repository;

import com.example.boardpjt.model.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByTitleContainingOrContentContaining(
            String title, String content, Pageable pageable);

    Page<Post> findByTitleContainingOrContentContainingOrderByIdDesc(
            String title, String content, Pageable pageable);

    Page<Post> findByAuthor_UsernameOrderByIdDesc(String username, Pageable pageable);

    Page<Post> findByAuthor_UsernameContaining(String username, Pageable pageable);

    Page<Post> findByAuthor_UsernameContainingOrderByIdDesc(String username, Pageable pageable);

    Page<Post> findByCategoryAndTitleContainingOrCategoryAndContentContaining(
            String category1, String title, String category2, String content, Pageable pageable);

    Page<Post> findByCategoryAndTitleContainingOrCategoryAndContentContainingOrderByIdDesc(
            String category1, String title, String category2, String content, Pageable pageable);

    Page<Post> findByCategory(String category, Pageable pageable);

    Page<Post> findByCategoryOrderByIdDesc(String category, Pageable pageable);

    Page<Post> findByCategoryAndAuthor_UsernameContaining(String category, String username, Pageable pageable);

    Page<Post> findByCategoryAndAuthor_UsernameContainingOrderByIdDesc(String category, String username, Pageable pageable);

    Page<Post> findByIdIn(List<Long> ids, Pageable pageable);

    // 인기순 정렬 (좋아요 수)
    @Query("SELECT p FROM Post p LEFT JOIN p.likes pl GROUP BY p ORDER BY COUNT(pl) DESC, p.id DESC")
    Page<Post> findAllOrderByLikeCount(Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN p.likes pl WHERE p.author.username LIKE %:username% GROUP BY p ORDER BY COUNT(pl) DESC, p.id DESC")
    Page<Post> findByAuthor_UsernameContainingOrderByLikeCount(@Param("username") String username, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN p.likes pl WHERE p.title LIKE %:keyword% OR p.content LIKE %:keyword% GROUP BY p ORDER BY COUNT(pl) DESC, p.id DESC")
    Page<Post> findByTitleContainingOrContentContainingOrderByLikeCount(@Param("keyword") String keyword1, @Param("keyword") String keyword2, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN p.likes pl WHERE p.category = :category GROUP BY p ORDER BY COUNT(pl) DESC, p.id DESC")
    Page<Post> findByCategoryOrderByLikeCount(@Param("category") String category, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN p.likes pl WHERE p.category = :category AND p.author.username LIKE %:username% GROUP BY p ORDER BY COUNT(pl) DESC, p.id DESC")
    Page<Post> findByCategoryAndAuthor_UsernameContainingOrderByLikeCount(@Param("category") String category, @Param("username") String username, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN p.likes pl WHERE p.category = :category AND (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) GROUP BY p ORDER BY COUNT(pl) DESC, p.id DESC")
    Page<Post> findByCategoryAndTitleContainingOrCategoryAndContentContainingOrderByLikeCount(
            @Param("category") String category1, @Param("keyword") String keyword1,
            @Param("category") String category2, @Param("keyword") String keyword2, Pageable pageable);

    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.postTags LEFT JOIN FETCH p.author WHERE p.id = :id")
    Optional<Post> findByIdWithTags(@Param("id") Long id);
}
