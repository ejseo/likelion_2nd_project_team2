# Travelog 프로젝트 문서

## 포함 문서

### 1. 인프라 설계 (LLD)
- docs/infra-lld.md

### 2. DB 설계 문서 + 스키마
- docs/db-design.md
- db/schema.sql

### 3. DevOps 계획서
- docs/devops-plan.md

---

## 프로젝트 개요
Travelog는 여행 리뷰를 올리고, 사진을 공유하고, 좋아요/북마크할 수 있는 플랫폼이다.

AWS 기반 서버리스·컨테이너 아키텍처를 사용하며 다음을 포함한다.

- CloudFront + S3 (정적 리소스)
- ECS Fargate + ALB (백엔드)
- RDS MySQL (DB)
- IAM, VPC, SG, CloudWatch

---

## 문서 작성 규칙
- 모든 리소스는 서울 리전(ap-northeast-2)
- 보안그룹은 최소 권한 원칙 준수
- DB/Infra/DevOps 문서는 정합성 유지
