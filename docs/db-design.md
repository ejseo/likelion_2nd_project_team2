# Travelog DB 설계서

## 1. 개요
Travelog는 여행 리뷰를 공유하는 서비스이며 다음 기능을 포함한다.

- 회원가입 / 로그인
- 리뷰 작성 (제목/본문/평점/카테고리/태그)
- 리뷰 이미지 다중 업로드
- 댓글 / 대댓글
- 좋아요
- 북마크

---

## 2. 테이블 목록
- user_account
- post
- post_image
- comment
- post_like
- post_bookmark

---

## 3. 테이블 상세 정의

### user_account
| 컬럼 | 타입 | 설명 |
|------|--------|--------|
| id | BIGINT PK | |
| email | VARCHAR(100) UNIQUE | |
| password | VARCHAR(255) | |
| nickname | VARCHAR(50) | |
| role | VARCHAR(20) | USER/ADMIN |
| created_at | DATETIME | |
| updated_at | DATETIME | |

---

### post
| 컬럼 | 타입 | 설명 |
|------|--------|--------|
| id | BIGINT PK | |
| user_id | BIGINT FK | 작성자 |
| title | VARCHAR(100) | |
| content | TEXT | |
| rating | INT | 1~5 |
| category | VARCHAR(50) | |
| tags | VARCHAR(200) | 쉼표 구분 |
| created_at | DATETIME | |
| updated_at | DATETIME | |

---

### post_image (다중 이미지)
| 컬럼 | 타입 | 설명 |
|------|--------|--------|
| id | BIGINT PK | |
| post_id | BIGINT FK | |
| image_url | VARCHAR(255) | CloudFront URL |
| sort_order | INT | 정렬 |
| created_at | DATETIME | |

---

### comment
| 컬럼 | 타입 | 설명 |
|------|--------|--------|
| id | BIGINT PK | |
| post_id | BIGINT FK | |
| user_id | BIGINT FK | |
| parent_comment_id | BIGINT FK NULL | 대댓글 |
| content | TEXT | |
| created_at | DATETIME | |

---

### post_like
| 컬럼 | 타입 | 설명 |
|------|--------|--------|
| post_id | BIGINT PK | |
| user_id | BIGINT PK | |
| created_at | DATETIME | |

---

### post_bookmark
| 컬럼 | 타입 | 설명 |
|------|--------|--------|
| post_id | BIGINT PK | |
| user_id | BIGINT PK | |
| created_at | DATETIME | |

---

## 4. 인덱스 설계
- post(category, created_at DESC, user_account_id)
- comment(post_id, created_at)
- user_follow(following_id)
- post_like(post_id)
- bookmark(user_account_id, post_id)
- post_tag(tag_name, post_id)