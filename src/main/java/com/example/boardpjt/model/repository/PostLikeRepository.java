package com.example.boardpjt.model.repository;

import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.PostLike;
import com.example.boardpjt.model.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    Optional<PostLike> findByUserAccountAndPost(UserAccount userAccount, Post post);
    long countByPost(Post post);
    List<PostLike> findByUserAccount(UserAccount userAccount);
}
