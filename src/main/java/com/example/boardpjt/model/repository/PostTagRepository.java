package com.example.boardpjt.model.repository;

import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.PostTag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    List<PostTag> findByPost(Post post);
    List<PostTag> findByTagName(String tagName);

    // 제안: 가장 많이 사용된 태그를 순서대로 조회하는 쿼리 추가
    @Query("SELECT pt.tagName FROM PostTag pt GROUP BY pt.tagName ORDER BY COUNT(pt.tagName) DESC")
    List<String> findPopularTags(Pageable pageable);
}
