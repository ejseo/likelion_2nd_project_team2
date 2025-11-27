package com.example.boardpjt.model.dto;

import com.example.boardpjt.model.entity.Post;
import com.example.boardpjt.model.entity.PostTag;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PostDTO {

    @Getter
    @Setter
    public static class Request {
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        private String title;

        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        private String content;

        private String imageUrl;
        private String category;
        private Integer rating;
        private List<String> tags;
        // 이미지 삭제 여부를 전달받기 위한 필드
        private boolean deleteImage = false;
    }

    public record Response(
            Long id,
            String title,
            String content,
            String username,
            LocalDateTime createdAt,
            String imageUrl,
            String category,
            Integer rating,
            long likeCount,
            List<String> tags
    ) {
        public static Response from(Post post, long likeCount) {
            return new Response(
                    post.getId(),
                    post.getTitle(),
                    post.getContent(),
                    post.getAuthor().getUsername(),
                    post.getCreatedAt(),
                    post.getImageUrl(),
                    post.getCategory(),
                    post.getRating(),
                    likeCount,
                    post.getPostTags().stream()
                            .map(PostTag::getTagName)
                            .collect(Collectors.toList())
            );
        }
    }
}
