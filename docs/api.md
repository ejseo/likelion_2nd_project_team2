# API 명세서 (초안)

## 1. 기본 원칙

- **Base URL**: `/api`
- **Content-Type**: `application/json`
- **인증**: 로그인이 필요한 API는 요청 헤더에 `Authorization: Bearer {JWT}` 토큰을 포함하여 전송합니다.

---

## 2. User API

### 2.1. 회원가입

- **Method**: `POST`
- **URL**: `/api/users/signup`
- **설명**: 새로운 사용자를 등록합니다.
- **Request Body**:
  ```json
  {
    "username": "user123",
    "password": "password1234"
  }
  ```
- **Response (Success: 201 Created)**:
  ```json
  {
    "userId": 1,
    "username": "user123"
  }
  ```
- **Response (Error)**:
  - `409 Conflict`: 이미 존재하는 사용자 이름일 경우

### 2.2. 로그인

- **Method**: `POST`
- **URL**: `/api/users/login`
- **설명**: 사용자 로그인 후 JWT 토큰을 발급합니다.
- **Request Body**:
  ```json
  {
    "username": "user123",
    "password": "password1234"
  }
  ```
- **Response (Success: 200 OK)**:
  ```json
  {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
  ```
- **Response (Error)**:
  - `401 Unauthorized`: 아이디 또는 비밀번호가 일치하지 않을 경우

---

## 3. Post API

### 3.1. 게시글 전체 조회

- **Method**: `GET`
- **URL**: `/api/posts`
- **설명**: 모든 게시글 목록을 조회합니다. (페이지네이션 고려: `/api/posts?page=0&size=10`)
- **Response (Success: 200 OK)**:
  ```json
  [
    {
      "postId": 1,
      "title": "첫 번째 게시글",
      "author": "user123",
      "createdAt": "2023-10-27T10:00:00Z"
    },
    {
      "postId": 2,
      "title": "두 번째 게시글",
      "author": "user456",
      "createdAt": "2023-10-27T11:00:00Z"
    }
  ]
  ```

### 3.2. 게시글 상세 조회

- **Method**: `GET`
- **URL**: `/api/posts/{postId}`
- **설명**: 특정 ID의 게시글을 상세 조회합니다.
- **Response (Success: 200 OK)**:
  ```json
  {
    "postId": 1,
    "title": "첫 번째 게시글",
    "content": "이것은 첫 번째 게시글의 내용입니다.",
    "author": "user123",
    "createdAt": "2023-10-27T10:00:00Z",
    "likeCount": 15,
    "tags": ["java", "spring"],
    "comments": [
      {
        "commentId": 101,
        "author": "user789",
        "content": "좋은 글이네요!",
        "createdAt": "2023-10-27T10:05:00Z"
      }
    ]
  }
  ```
- **Response (Error)**:
  - `404 Not Found`: 해당 ID의 게시글이 없을 경우

### 3.3. 게시글 작성

- **Method**: `POST`
- **URL**: `/api/posts`
- **인증**: 필요 (Bearer Token)
- **설명**: 새로운 게시글을 작성합니다.
- **Request Body**:
  ```json
  {
    "title": "새로운 게시글 제목",
    "content": "새로운 게시글 내용입니다.",
    "tags": ["java", "spring", "api"]
  }
  ```
- **Response (Success: 201 Created)**:
  ```json
  {
    "postId": 3,
    "title": "새로운 게시글 제목",
    "content": "새로운 게시글 내용입니다.",
    "author": "currentUser",
    "createdAt": "2023-10-27T12:00:00Z",
    "tags": ["java", "spring", "api"]
  }
  ```

### 3.4. 게시글 수정

- **Method**: `PUT`
- **URL**: `/api/posts/{postId}`
- **인증**: 필요 (작성자 본인)
- **설명**: 특정 ID의 게시글을 수정합니다.
- **Request Body**:
  ```json
  {
    "title": "수정된 게시글 제목",
    "content": "수정된 게시글 내용입니다.",
    "tags": ["java", "spring-boot"]
  }
  ```
- **Response (Success: 200 OK)**:
  ```json
  {
    "postId": 3,
    "title": "수정된 게시글 제목",
    "content": "수정된 게시글 내용입니다.",
    "author": "currentUser",
    "createdAt": "2023-10-27T12:00:00Z",
    "updatedAt": "2023-10-27T12:30:00Z"
  }
  ```
- **Response (Error)**:
  - `403 Forbidden`: 수정 권한이 없을 경우
  - `404 Not Found`: 해당 ID의 게시글이 없을 경우

### 3.5. 게시글 삭제

- **Method**: `DELETE`
- **URL**: `/api/posts/{postId}`
- **인증**: 필요 (작성자 본인)
- **설명**: 특정 ID의 게시글을 삭제합니다.
- **Response (Success: 204 No Content)**:
- **Response (Error)**:
  - `403 Forbidden`: 삭제 권한이 없을 경우
  - `404 Not Found`: 해당 ID의 게시글이 없을 경우

### 3.6. 태그로 게시글 검색

- **Method**: `GET`
- **URL**: `/api/posts/search?tag={tagName}`
- **설명**: 특정 태그를 포함하는 모든 게시글 목록을 조회합니다.
- **Response (Success: 200 OK)**: (게시글 전체 조회와 동일한 형식)

---

## 4. Comment API

### 4.1. 댓글 작성

- **Method**: `POST`
- **URL**: `/api/posts/{postId}/comments`
- **인증**: 필요 (Bearer Token)
- **설명**: 특정 게시글에 새로운 댓글을 작성합니다.
- **Request Body**:
  ```json
  {
    "content": "새로운 댓글 내용입니다."
  }
  ```
- **Response (Success: 201 Created)**:
  ```json
  {
    "commentId": 102,
    "author": "currentUser",
    "content": "새로운 댓글 내용입니다.",
    "createdAt": "2023-10-27T13:00:00Z"
  }
  ```
- **Response (Error)**:
  - `404 Not Found`: 부모 게시글이 없을 경우

### 4.2. 댓글 수정

- **Method**: `PUT`
- **URL**: `/api/posts/{postId}/comments/{commentId}`
- **인증**: 필요 (작성자 본인)
- **설명**: 특정 댓글을 수정합니다.
- **Request Body**:
  ```json
  {
    "content": "수정된 댓글 내용입니다."
  }
  ```
- **Response (Success: 200 OK)**:
  ```json
  {
    "commentId": 102,
    "author": "currentUser",
    "content": "수정된 댓글 내용입니다.",
    "updatedAt": "2023-10-27T13:30:00Z"
  }
  ```
- **Response (Error)**:
  - `403 Forbidden`: 수정 권한이 없을 경우
  - `404 Not Found`: 해당 댓글이 없을 경우

### 4.3. 댓글 삭제

- **Method**: `DELETE`
- **URL**: `/api/posts/{postId}/comments/{commentId}`
- **인증**: 필요 (작성자 본인)
- **설명**: 특정 댓글을 삭제합니다.
- **Response (Success: 204 No Content)**:
- **Response (Error)**:
  - `403 Forbidden`: 삭제 권한이 없을 경우
  - `404 Not Found`: 해당 댓글이 없을 경우

---

## 5. Post Like API

### 5.1. 게시글 좋아요 / 좋아요 취소

- **Method**: `POST`
- **URL**: `/api/posts/{postId}/like`
- **인증**: 필요 (Bearer Token)
- **설명**: 특정 게시글에 '좋아요'를 추가하거나 취소합니다. (토글 방식)
- **Response (Success: 200 OK)**:
  ```json
  {
    "message": "게시글 좋아요를 처리했습니다.",
    "likeCount": 15
  }
  ```
- **Response (Error)**:
  - `404 Not Found`: 해당 ID의 게시글이 없을 경우

---

## 6. Bookmark API

### 6.1. 북마크 추가 / 취소

- **Method**: `POST`
- **URL**: `/api/posts/{postId}/bookmark`
- **인증**: 필요 (Bearer Token)
- **설명**: 특정 게시글을 자신의 북마크에 추가하거나 삭제합니다. (토글 방식)
- **Response (Success: 200 OK)**:
  ```json
  {
    "message": "북마크를 처리했습니다."
  }
  ```
- **Response (Error)**:
  - `404 Not Found`: 해당 ID의 게시글이 없을 경우

### 6.2. 내 북마크 목록 조회

- **Method**: `GET`
- **URL**: `/api/bookmarks`
- **인증**: 필요 (Bearer Token)
- **설명**: 현재 사용자가 북마크한 모든 게시글 목록을 조회합니다.
- **Response (Success: 200 OK)**: (게시글 전체 조회와 동일한 형식)
