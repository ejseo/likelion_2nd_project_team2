package com.example.boardpjt.model.repository;

import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostTagRepository extends JpaRepository<PostTag, Long> {
    List<PostTag> findByPost(Post post);
    List<PostTag> findByTagName(String tagName);
}
