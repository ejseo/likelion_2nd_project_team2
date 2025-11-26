package com.example.boardpjt.model.repository;

import com.example.boardpjt.model.entity.Bookmark;
import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    Optional<Bookmark> findByUserAccountAndPost(UserAccount userAccount, Post post);
    List<Bookmark> findByUserAccount(UserAccount userAccount);
}
