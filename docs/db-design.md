# Travelog DB 설계서

## 1. 핵심 테이블 목록

| 테이블명 | 엔티티 | 설명 |
|---------|--------|------|
| user_account | UserAccount | 사용자 계정 정보 |
| post | Post | 게시글(여행 리뷰) 정보 |
| comment | Comment | 댓글 정보 |
| user_follow | UserFollow | 팔로우 관계 |
| post_like | PostLike | 게시글 좋아요 |
| bookmark | Bookmark | 게시글 북마크 |
| post_tag | PostTag | 게시글 태그 |

---

## 2. 테이블 관계 요약

- user_account 1 : N post
- user_account 1 : N comment
- post 1 : N comment
- user_account N : M user_follow
- user_account 1 : N post_like
- post 1 : N post_like
- user_account 1 : N bookmark

---

## 3. 주요 테이블 속성 (요약)

### user_account
- id
- username
- email
- password

### post
- id
- user_id
- title
- content
- rating
- image_url

### comment
- id
- post_id
- user_id
- content

(※ 나머지 DDL은 schema.sql 참조)