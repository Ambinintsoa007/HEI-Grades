# HEI Grades

HEI Grades is a POJA-based Spring Boot application used to manage HEI students,
teachers, academic grades, transcripts and graduates.

## Authors

- RAZAFIMAMONJY Rocques NyAina Giovanni Nelio — K2 — STD24114
- RANDRIANARIVELO Ambinintsoa Dylan Nomenjanahary — K2 — STD24214

## Main features

- STUDENT, TEACHER and ADMIN authentication
- student academic group history
- course and teacher assignments
- exam and grade management
- grade correction history
- student transcripts
- promotion results over three academic years
- graduate calculation
- PDF transcript generation
- asynchronous email delivery
- AWS S3 file storage
- XLSX graduate export
- Thymeleaf administration interface

## Stack

- Java 21
- Spring Boot 3.2.2
- PostgreSQL
- Flyway
- Spring Data JPA
- Spring Security
- JWT
- POJA
- AWS Lambda
- AWS EventBridge
- AWS SQS
- AWS S3
- AWS SES
- Testcontainers
- JaCoCo

## Architecture

```text
endpoint/
  rest/
    controller/

service/

model/

repository/
  model/

mapper/

endpoint/event/
service/event/