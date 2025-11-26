# 아키텍처 설계서 v1

## 1. 요청 흐름

1. 사용자가 Travelog에 접속한다.
2. 정적 콘텐츠 및 이미지는 CloudFront가 제공한다.
3. 일반 웹 요청은 Application Load Balancer(ALB)로 전달된다.
4. ALB는 ECS Fargate Task로 트래픽을 전달한다.
5. Fargate Task 내부 애플리케이션은 RDS(MySQL)와 통신한다.
6. 업로드된 이미지 파일은 S3에 저장된다.

---

## 2. 전체 구성 요소

- Amazon Route 53 (DNS 라우팅)
- Amazon CloudFront (정적 콘텐츠 캐싱)
- Application Load Balancer
- ECS Fargate Task
- Amazon RDS (MySQL)
- Amazon S3 (이미지 저장)
- Amazon ECR (Docker 이미지 저장소)
- IAM Role (Task Role, Execution Role)
- NAT Gateway (외부 API 요청 시)
- Internet Gateway

---

## 3. 서비스 구조

### 3.1 ECS Service
- 도커 이미지는 ECR에서 가져온다.
- 애플리케이션 로그는 CloudWatch Logs로 전송된다.
- 환경변수는 Secrets Manager를 통해 로딩한다.

### 3.2 Fargate Task 내 기능
- 리뷰 이미지 저장을 위한 S3 접근
- 리뷰 데이터 저장을 위한 RDS 접근

---

## 4. 데이터 흐름 예시

### 리뷰 게시 요청
1. 사용자 → CloudFront → ALB → Fargate
2. 이미지 업로드 → S3 저장
3. 리뷰 정보 저장 → RDS(MySQL)
4. 응답 반환 → CloudFront → 사용자

---

## 5. 네트워크 구성

- Public Subnet: ALB, NAT Gateway
- Private Subnet: ECS Fargate, RDS
- Route Table: IGW/NAT 경로 설정