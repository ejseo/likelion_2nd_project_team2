# Travelog 프로젝트 DB 설계서

본 문서는 Travelog 프로젝트의 데이터베이스 논리 설계를 정의한다.  
Spring Boot + JPA 기반의 게시판/리뷰 시스템으로, 사용자 계정, 게시물, 댓글, 팔로우, 좋아요, 북마크, 태그 기능을 포함한다.

---

## 1. 핵심 테이블 요약

| 테이블명      | 엔티티       | 설명                          |
|--------------|-------------|-------------------------------|
| user_account | UserAccount | 사용자 계정 정보              |
| post         | Post        | 게시물(여행 리뷰) 정보        |
| comment      | Comment     | 댓글 정보                     |
| user_follow  | (중간 테이블) | 팔로우 관계 (N:M)            |
| post_like    | PostLike    | 게시물 좋아요                 |
| bookmark     | Bookmark    | 게시물 북마크                 |
| post_tag     | PostTag     | 게시물 태그(정규화)           |

---

## 2. ERD (Entity Relationship Diagram)

### 2.1 테이블 관계 요약

- user_account 1 : N post
- user_account 1 : N comment
- post 1 : N comment
- user_account N : M user_follow (follower ↔ following)
- user_account 1 : N post_like
- post 1 : N post_like
- user_account 1 : N bookmark
- post 1 : N bookmark
- post 1 : N post_tag

| 부모 테이블   | 자식 테이블 | 관계 | FK 컬럼                    | 설명                   |
|--------------|-------------|------|----------------------------|------------------------|
| user_account | post        | 1:N  | user_account_id            | 사용자→게시물          |
| user_account | comment     | 1:N  | user_account_id            | 사용자→댓글            |
| post         | comment     | 1:N  | post_id                    | 게시물→댓글            |
| user_account | user_follow | N:M  | follower_id, following_id  | 팔로우 관계            |
| user_account | post_like   | 1:N  | user_account_id            | 사용자→좋아요          |
| post         | post_like   | 1:N  | post_id                    | 게시물→좋아요          |
| user_account | bookmark    | 1:N  | user_account_id            | 사용자→북마크          |
| post         | bookmark    | 1:N  | post_id                    | 게시물→북마크          |
| post         | post_tag    | 1:N  | post_id                    | 게시물→태그            |

---

## 3. 테이블 정의

### 3.1 user_account (사용자 계정)

| 컬럼명      | 데이터 타입   | NULL     | Key  | 설명                                  |
|------------|---------------|----------|------|---------------------------------------|
| id         | BIGINT        | NOT NULL | PK   | AUTO_INCREMENT                        |
| username   | VARCHAR(50)   | NOT NULL | UK   | 사용자명 (고유)                       |
| password   | VARCHAR(255)  | NOT NULL | -    | 암호화된 비밀번호                     |
| role       | VARCHAR(20)   | NOT NULL | -    | 역할 (ROLE_USER, ROLE_ADMIN)         |
| provider   | VARCHAR(50)   | NULL     | -    | 소셜 로그인 제공자 (kakao 등)        |
| created_at | DATETIME      | NOT NULL | -    | 생성일시 (자동)                       |
| updated_at | DATETIME      | NOT NULL | -    | 수정일시 (자동)                       |

---

### 3.2 post (게시물)

| 컬럼명          | 데이터 타입    | NULL     | Key | 설명                                      |
|-----------------|----------------|----------|-----|-------------------------------------------|
| id              | BIGINT         | NOT NULL | PK  | AUTO_INCREMENT                            |
| title           | VARCHAR(200)   | NOT NULL | -   | 게시물 제목                               |
| content         | TEXT           | NOT NULL | -   | 게시물 내용                               |
| image_url       | VARCHAR(255)   | NULL     | -   | 이미지 URL                                |
| category        | VARCHAR(50)    | NULL     | -   | 카테고리 (beach, mountain 등)            |
| rating          | INT            | NULL     | -   | 평점 (1-5)                                |
| user_account_id | BIGINT         | NOT NULL | FK  | 작성자 ID → user_account(id)             |
| created_at      | DATETIME       | NOT NULL | -   | 생성일시                                  |
| updated_at      | DATETIME       | NOT NULL | -   | 수정일시                                  |

---

### 3.3 comment (댓글)

| 컬럼명          | 데이터 타입 | NULL     | Key | 설명                                      |
|-----------------|-------------|----------|-----|-------------------------------------------|
| id              | BIGINT      | NOT NULL | PK  | AUTO_INCREMENT                            |
| content         | TEXT        | NOT NULL | -   | 댓글 내용                                 |
| user_account_id | BIGINT      | NOT NULL | FK  | 작성자 ID → user_account(id)             |
| post_id         | BIGINT      | NOT NULL | FK  | 게시물 ID → post(id)                      |
| created_at      | DATETIME    | NOT NULL | -   | 생성일시                                  |
| updated_at      | DATETIME    | NOT NULL | -   | 수정일시                                  |

---

### 3.4 user_follow (팔로우 관계)

| 컬럼명      | 데이터 타입 | NULL     | Key      | 설명                                   |
|-------------|-------------|----------|----------|----------------------------------------|
| follower_id | BIGINT      | NOT NULL | PK, FK   | 팔로우 하는 사람 → user_account(id)   |
| following_id| BIGINT      | NOT NULL | PK, FK   | 팔로우 당하는 사람 → user_account(id) |

---

### 3.5 post_like (좋아요)

| 컬럼명          | 데이터 타입 | NULL     | Key        | 설명                                      |
|-----------------|-------------|----------|------------|-------------------------------------------|
| id              | BIGINT      | NOT NULL | PK         | AUTO_INCREMENT                            |
| user_account_id | BIGINT      | NOT NULL | FK, UK     | 좋아요 누른 사용자 → user_account(id)    |
| post_id         | BIGINT      | NOT NULL | FK, UK     | 좋아요 대상 게시물 → post(id)            |
| created_at      | DATETIME    | NOT NULL | -          | 좋아요 시간                               |

> UNIQUE KEY: (user_account_id, post_id) – 사용자당 게시물 하나에 좋아요 1회만 가능

---

### 3.6 bookmark (북마크)

| 컬럼명          | 데이터 타입 | NULL     | Key        | 설명                                       |
|-----------------|-------------|----------|------------|--------------------------------------------|
| id              | BIGINT      | NOT NULL | PK         | AUTO_INCREMENT                             |
| user_account_id | BIGINT      | NOT NULL | FK, UK     | 북마크한 사용자 → user_account(id)        |
| post_id         | BIGINT      | NOT NULL | FK, UK     | 북마크 대상 게시물 → post(id)             |
| created_at      | DATETIME    | NOT NULL | -          | 북마크 시간                                |

> UNIQUE KEY: (user_account_id, post_id) – 사용자당 게시물 하나에 북마크 1회만 가능

---

### 3.7 post_tag (게시물 태그)

| 컬럼명     | 데이터 타입 | NULL     | Key        | 설명                           |
|-----------|-------------|----------|------------|--------------------------------|
| id        | BIGINT      | NOT NULL | PK         | AUTO_INCREMENT                 |
| post_id   | BIGINT      | NOT NULL | FK, UK     | 게시물 ID → post(id)          |
| tag_name  | VARCHAR(50) | NOT NULL | UK         | 태그명                         |
| created_at| DATETIME    | NOT NULL | -          | 태그 생성일시                  |

> UNIQUE KEY: (post_id, tag_name) – 게시물당 동일 태그 중복 불가

---

## 4. 인덱스 설계

| 인덱스명             | 테이블     | 컬럼             | 용도                          |
|----------------------|-----------|------------------|-------------------------------|
| idx_post_category    | post      | category         | 카테고리별 조회               |
| idx_post_created_at  | post      | created_at DESC  | 최신순 정렬                   |
| idx_post_user_account| post      | user_account_id  | 사용자별 게시물 조회          |
| idx_comment_post_id  | comment   | post_id          | 게시물별 댓글 조회            |
| idx_comment_created  | comment   | created_at       | 댓글 시간순 정렬              |
| idx_follow_following | user_follow| following_id    | 팔로워 목록 조회              |
| idx_like_post_id     | post_like | post_id          | 게시물별 좋아요 수 조회       |
| idx_bookmark_user    | bookmark  | user_account_id  | 사용자별 북마크 목록          |
| idx_bookmark_post    | bookmark  | post_id          | 게시물별 북마크 수 조회       |
| idx_post_tag_user    | post_tag  | tag_name         | 태그명 검색                   |
| idx_post_tag_post_id | post_tag  | post_id          | 게시물별 태그 조회            |

---

## 5. DDL 스크립트 (요약 예시)

> 실제 DB 생성 시에는 원본 설계서에 포함된 DDL 스크립트를 사용한다.  
> 아래는 user_account 테이블 예시이며, 나머지 테이블(post, comment, user_follow, post_like, bookmark, post_tag)도 동일한 설계에 맞춰 생성한다.

```sql
CREATE TABLE user_account (
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    username        VARCHAR(50)  NOT NULL,
    password        VARCHAR(255) NOT NULL,
    role            VARCHAR(20)  NOT NULL DEFAULT 'ROLE_USER',
    provider        VARCHAR(50)  NULL,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                 ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;