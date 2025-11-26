# 인프라 상세 설계서

## 1. VPC

- CIDR: 10.0.0.0/16

---

## 2. Subnet

### Public Subnet
- ALB와 NAT Gateway 배치

### Private Subnet (App)
- ECS Fargate 배치

### Private Subnet (DB)
- RDS(MySQL) 배치

---

## 3. Route Table

### Public Route Table
- 0.0.0.0/0 → Internet Gateway

### Private Route Table
- 0.0.0.0/0 → NAT Gateway

---

## 4. Internet Gateway

- 외부 트래픽을 위한 인터넷 출입구 역할

---

## 5. NAT Gateway

- Private Subnet에서 외부로 나가는 요청 처리

---

## 6. Security Group

### ALB Security Group
- 80/443 포트 허용

### App Security Group (ECS)
- ALB에서 오는 트래픽만 허용

### DB Security Group
- ECS Security Group에서 오는 MySQL(3306)만 허용

---

## 7. RDS

- 엔진: MySQL
- Subnet Group: Private Subnet
- 접근: 전용 DB Security Group

---

## 8. IAM Role

### Execution Role
- AmazonECSTaskExecutionRolePolicy
    - CloudWatch Logs 전송
    - ECR Pull

### Task Role
- S3 접근(PutObject, GetObject)
- RDS 접근

---

## 9. S3

- 리뷰 이미지 파일 저장
- CloudFront 오리진으로 연결 가능