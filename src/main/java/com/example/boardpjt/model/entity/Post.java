package com.example.boardpjt.model.entity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Post extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    // [추가] 이미지 파일 URL을 저장할 필드
    private String imageUrl;

    // [추가] 카테고리 (beach, mountain, city, food, culture, activity)
    private String category;

    // [추가] 평점 (1-5)
    private Integer rating;

    // [추가] 태그 (쉼표로 구분된 문자열)
    @Column(length = 500)
    private String tags;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_account_id", nullable = false)
    private UserAccount author;
}