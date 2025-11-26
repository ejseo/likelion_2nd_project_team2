# Travelog 프로젝트 개요서

## 1. 서비스 개요

Travelog는 여행 경험을 기록하고 공유하는 여행지 리뷰 서비스이다.  
사용자는 여행 후기를 작성하고, 사진을 업로드하며, 다른 사용자의 리뷰를 확인할 수 있다.

---

## 2. 주요 기능

### 2.1 로그인 및 회원가입
- 서비스 이용을 위한 기본 로그인 기능 제공

### 2.2 리뷰 게시 기능
- 리뷰 작성 및 편집
- 제목, 본문, 평점 입력
- 여행지 사진 업로드
- 카테고리, 태그 설정

### 2.3 사용자 상호작용
- 댓글 및 대댓글
- 좋아요
- 북마크
- 마이페이지

### 2.4 정보 검색
- 제목·내용 검색
- 카테고리 및 평점 기반 탐색

---

## 3. 기술 스택

### Backend
- Spring Boot 3.x
- Java 17
- Spring Web MVC
- Spring Data JPA
- Gradle
- Lombok

### Frontend
- JSP
- HTML / CSS / JavaScript

### Database
- Amazon RDS (MySQL)

### Infra
- VPC / Subnet / Route Table / Internet Gateway
- Application Load Balancer
- ECS Fargate
- ECR
- S3
- CloudFront
- IAM (Task Role, Execution Role)

---

## 4. 타겟 사용자

- 1~2인 소규모 여행자
- 여행 콘텐츠 제작자
- 관광 업계 종사자 및 관련 업체

---

## 5. 사용 시나리오

### 여행 정보 탐색
1. 로그인
2. 목적지 검색
3. 리뷰·좋아요·댓글 확인
4. 북마크로 저장

### 리뷰 작성
1. 로그인
2. 제목·본문 입력
3. 사진 업로드
4. 공유

### 상호작용
- 좋아요
- 댓글 작성

---

## 6. 선택 기능

### S3 + CloudFront
- 리뷰 이미지 저장
- 정적 파일 제공
- 이미지 로딩 속도 개선

### DevOps 및 IaC 적용
- GitHub Actions 기반 자동화
- Docker 이미지 빌드 및 ECR 푸시
- ECS 서비스 업데이트
- 인프라를 코드 형태로 관리