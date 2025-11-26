# AWS 인프라 구축 결과

본 문서는 AWS Management Console에서 확인된 구성 요소의 상태를 정리한 것이다.

## 1. VPC

- VPC 생성 완료
- CIDR: 10.0.0.0/16

## 2. Subnet

- Public Subnet 2개
- Private Subnet(App) 2개
- Private Subnet(DB) 2개

## 3. Security Group

- ALB SG 생성
- ECS SG 생성
- DB SG 생성

## 4. RDS

- MySQL 인스턴스 생성 완료 (Private Subnet)
- DB 전용 SG 적용됨