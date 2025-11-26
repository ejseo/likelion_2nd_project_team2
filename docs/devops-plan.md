# DevOps 계획서 (CI/CD + ECS 배포)

## 1. 목적
- Spring Boot → Docker → ECR → ECS(Fargate) 자동배포 구축
- CloudWatch 기반 모니터링/알람 적용

---

## 2. Docker & ECR
### Dockerfile
- JDK17
- JAR → /app/app.jar 복사
- EXPOSE 8080

### ECR Repository
- travelog-backend
- GitHub Actions push 시 자동 업데이트

---

## 3. GitHub Actions

### CI (build.yml)
- JDK 17
- Gradle build
- Docker build & push to ECR

### CD (deploy.yml)
- ECS Task Definition 업데이트
- ECS Service 신규 리비전 배포

---

## 4. ECS 운영 전략
- Rolling Update
- 최소/최대 가용성 설정
- CPU/Memory 알람

---

## 5. CloudWatch
### Logs
- /ecs/travelog/app 로그 그룹 생성

### Metrics/Alarms
- ECS CPU/Memory
- ALB 5xx 에러
- RDS CPU/Connections

---

## 6. 산출물
- Dockerfile
- GitHub Actions YAML
- ECS Task Definition
- 배포 구조도