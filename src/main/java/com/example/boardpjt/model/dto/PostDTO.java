package com.example.boardpjt.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

public class PostDTO {

    @Getter
    @Setter
    public static class Request {
        @NotBlank(message = "제목은 필수 입력 항목입니다.")
        private String title;

        @NotBlank(message = "내용은 필수 입력 항목입니다.")
        private String content;

        private String username;

        // [추가] 이미지 URL 필드
        private String imageUrl;
    }

    // [수정] Response record에 imageUrl 추가
    public record Response(
            Long id,
            String title,
            String content,
            String username,
            LocalDateTime createdAt,
            String imageUrl
    ) {}
}