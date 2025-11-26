# DevOps 구성 문서

## 1. Docker

- 애플리케이션을 Docker 이미지로 빌드
- 빌드된 이미지를 ECR에 저장

## 2. GitHub Actions

- GitHub Actions로 CI/CD 구현
- 코드 푸시 시 Docker 이미지 자동 빌드
- ECR에 자동 푸시
- ECS 서비스 자동 업데이트

## 3. IaC

- 인프라를 코드로 관리
- VPC, ECS, RDS, 보안그룹 등 구성 요소를 코드 기반으로 관리 가능