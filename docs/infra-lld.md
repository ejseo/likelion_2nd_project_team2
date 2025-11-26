# 클라우드 인프라 상세 설계서 (LLD, Low Level Design)

## 1. VPC 설계
- 리전: ap-northeast-2 (Seoul)
- VPC CIDR: 10.0.0.0/16

### 서브넷 구성
| 구분 | 이름 | AZ | CIDR | 용도 |
|------|------|------|------------|-------------|
| Public Subnet 1 | public-subnet-a | ap-northeast-2a | 10.0.1.0/24 | ALB / NAT |
| Public Subnet 2 | public-subnet-c | ap-northeast-2c | 10.0.2.0/24 | ALB / NAT |
| Private App 1 | private-app-a | ap-northeast-2a | 10.0.11.0/24 | ECS Fargate |
| Private App 2 | private-app-c | ap-northeast-2c | 10.0.12.0/24 | ECS Fargate |
| Private DB 1 | private-db-a | ap-northeast-2a | 10.0.21.0/24 | RDS |
| Private DB 2 | private-db-c | ap-northeast-2c | 10.0.22.0/24 | RDS |

---

## 2. 라우팅 구성

### Public Route Table
- 0.0.0.0/0 → IGW

### Private App Route Table
- 0.0.0.0/0 → NAT Gateway

### Private DB Route Table
- 외부 통신 없음

---

## 3. 보안그룹 설계

### ALB SG
- Inbound: 80/443 → 0.0.0.0/0
- Outbound: All

### ECS(Fargate) SG
- Inbound: 8080 → ALB SG만 허용
- Outbound: RDS SG, S3 허용

### RDS(MySQL) SG
- Inbound: 3306 → ECS SG만 허용
- 절대 0.0.0.0/0 허용 금지

---

## 4. S3 / CloudFront

### S3 버킷
- 버킷명: travelog-review-images
- 이미지 경로:
    - /reviews/{postId}/image_1.jpg

### CloudFront
- 오리진: S3
- OAI 적용
- TTL: 1일

---

## 5. RDS 설계
- 엔진: MySQL 8.x
- 인스턴스: db.t3.micro
- 스토리지: 20GB
- Multi-AZ: Off
- 백업: 1~3일
- 파라미터 그룹: utf8mb4 / Asia/Seoul

엔드포인트 예시:  
`travelog-db.xxxxx.ap-northeast-2.rds.amazonaws.com:3306`

---

## 6. ECS (Fargate)
- Task: 0.5 vCPU / 1GB
- Container Port: 8080
- Execution Role: ECR Pull
- Task Role: S3, RDS 접근권한 필요

---

## 7. 체크리스트
- 리전 검증: ap-northeast-2
- RDS Public Access: NO
- SG 체인: ALB → ECS → RDS
- 포트 3306 확인