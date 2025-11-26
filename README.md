# Travelog 프로젝트

Travelog는 여행 리뷰를 작성하고 사진을 공유할 수 있는 서비스이다.  
사용자는 리뷰 작성, 이미지 업로드, 댓글/좋아요/북마크를 이용해 여행 정보를 기록하고 다른 사람과 공유할 수 있다.  
본 프로젝트는 AWS 기반의 컨테이너 아키텍처로 구성되어 있으며, VPC·서브넷·ALB·ECS·RDS 등 다양한 AWS 서비스를 활용하여 배포된다.

---

## 1. 프로젝트 개요

Travelog는 다음 기능을 제공한다.

- 리뷰 작성/편집
- 여행지 사진 업로드
- 카테고리 및 태그 설정
- 댓글 및 대댓글
- 좋아요
- 북마크
- 제목 및 내용 검색
- 마이페이지

기술 스택은 Spring Boot, JSP, MySQL, AWS ECS·RDS·S3·CloudFront 등으로 구성된다.

---

## 2. 아키텍처 구성 요소

아키텍처는 PDF 산출물에서 정의된 구성 요소를 기반으로 한다.

- CloudFront + S3
    - 정적 파일 및 이미지 제공
- Application Load Balancer
    - HTTP 요청 처리
- ECS Fargate
    - Spring Boot 컨테이너 실행
- Amazon RDS(MySQL)
    - 리뷰/댓글/사용자 데이터 저장
- Amazon ECR
    - Docker 이미지 저장
- VPC / Subnet / Route Table / IGW / NAT Gateway
- IAM Role (Execution Role, Task Role)
- CloudWatch Logs
    - ECS 컨테이너 로그 저장

---

## 3. 문서 목록 (docs)

프로젝트 산출물 PDF들을 기반으로 한 문서들이다.

### 프로젝트 요약 및 설계 문서
- [프로젝트 개요](./docs/project-overview.md)
- [아키텍처 설계서](./docs/architecture-v1.md)

### 인프라 설계
- [인프라 상세 설계서](./docs/infra-lld.md)

### DB 설계
- [DB 설계서](./docs/db-design.md)
- [DB 스키마(schema.sql)](./db/schema.sql)

### DevOps 구성
- [DevOps 구성 문서](./docs/devops-plan.md)

### 구축 결과
- [AWS 구축 결과](./docs/aws-result.md)
- [RDS 접속 테스트 증빙](./docs/rds-test.md)

---

## 4. 프로젝트 구조

아래는 실제 리포지토리 구조이다.

likelion_2nd_project_team2/
├── .github/
│   └── workflows/
│       ├── build.yml
│       └── deploy.yml
│
├── db/
│   └── schema.sql
│
├── docs/
│   ├── assets/
│   ├── architecture-v1.md
│   ├── aws-result.md
│   ├── db-design.md
│   ├── devops-plan.md
│   ├── infra-lld.md
│   ├── project-overview.md
│   └── rds-test.md
│
├── infrastructure/
│   ├── igw.yaml
│   ├── nat.yaml
│   ├── rds.yaml
│   ├── role.yaml
│   ├── route.yaml
│   ├── s3.yaml
│   ├── sg.yaml
│   ├── subnet.yaml
│   └── vpc.yaml
│
├── .gitignore 
├── Dockerfile
└── README.md

## 5. 배포 방식

배포 과정은 기획안 옵션으로 정의된 DevOps 구성을 따른다.

1. GitHub Actions에서 코드 푸시 감지
2. Docker 이미지 빌드
3. Amazon ECR에 이미지 푸시
4. ECS Fargate 서비스가 새로운 Task 정의로 업데이트
5. 서비스 자동 배포

---

## 6. 역할 분담

PDF 산출물에 정의된 역할 분담이다.

- **인프라(AWS)**: VPC, Subnet, ALB, ECS, RDS, S3
- **백엔드**: 리뷰/댓글/좋아요/북마크 기능 구현
- **DevOps & 운영 문서**: Docker, ECR, GitHub Actions, 문서 정리

---

## 7. 참고 문서

- 프로젝트 개요서 PDF
- 인프라 상세설계서 PDF
- DB 설계서 PDF
- AWS 구축 결과 캡처
- RDS 접속 테스트 캡처