# Swagger API 실습 가이드

이 문서는 현재 구현된 프로젝트 협업 서비스 API를 Swagger UI에서 순서대로 실행하기 위한 안내서입니다.

## 1. 접속 주소

- Swagger UI: <http://localhost:8080/swagger-ui/index.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs>
- H2 Console: <http://localhost:8080/h2-console>

페이지가 열리지 않으면 먼저 IntelliJ에서 `ProjectCollabApplication`을 실행합니다.

서버를 재실행하면 인메모리 H2 데이터베이스의 데이터와 ID가 모두 초기화됩니다. 따라서 아래 실습도 사용자 생성부터 다시 시작해야 합니다.

## 2. Swagger 사용 방법

각 API에서 다음 순서로 실행합니다.

1. 실행할 API 행을 클릭해 펼칩니다.
2. 오른쪽의 **Try it out** 버튼을 누릅니다.
3. 경로 변수, `X-Requester-Id`, Request body를 입력합니다.
4. **Execute** 버튼을 누릅니다.
5. `Server response`의 응답 코드와 `Response body`를 확인합니다.

아래 값을 메모하면서 진행하면 편합니다.

| 이름 | 의미 | 실습 중 기록할 값 |
| :-- | :-- | :-- |
| `ownerId` | 프로젝트를 생성할 첫 번째 사용자 ID | |
| `memberId` | 프로젝트에 추가할 두 번째 사용자 ID | |
| `projectId` | 생성한 프로젝트 ID | |
| `taskId` | 생성한 작업 ID | |

`X-Requester-Id`는 현재 요청을 수행하는 사용자의 ID입니다. 인증 기능 대신 이 헤더로 요청자를 전달합니다.

## 3. 권장 실습 순서

### 1단계: OWNER로 사용할 사용자 생성

Swagger의 **Users**에서 `POST /api/users`를 실행합니다.

```json
{
  "name": "이기문",
  "email": "owner@example.com"
}
```

정상 응답 코드는 `201`입니다. 응답의 `id`를 `ownerId`로 기록합니다.

```json
{
  "id": 1,
  "name": "이기문",
  "email": "owner@example.com"
}
```

위의 `id: 1`은 예시입니다. 실제 응답에 나온 값을 사용해야 합니다.

### 2단계: MEMBER로 사용할 사용자 생성

같은 `POST /api/users`를 다시 실행합니다.

```json
{
  "name": "김멤버",
  "email": "member@example.com"
}
```

응답의 `id`를 `memberId`로 기록합니다. 이메일은 중복될 수 없으므로 첫 번째 사용자와 다른 이메일을 입력해야 합니다.

### 3단계: 사용자 조회 확인

`GET /api/users/{userId}`를 실행합니다.

- `userId`: `ownerId`

응답 코드 `200`과 첫 번째 사용자 정보가 나오면 정상입니다.

### 4단계: 프로젝트 생성

Swagger의 **Projects**에서 `POST /api/projects`를 실행합니다.

- `X-Requester-Id`: `ownerId`

```json
{
  "name": "협업 서비스 개발",
  "description": "React와 Spring Boot를 사용하는 채용 과제"
}
```

정상 응답 코드는 `201`입니다. 응답의 `id`를 `projectId`로 기록합니다.

프로젝트를 생성한 사용자는 해당 프로젝트의 `OWNER` 멤버로 자동 등록됩니다.

### 5단계: OWNER 자동 등록 확인

Swagger의 **Project Members**에서 `GET /api/projects/{projectId}/members`를 실행합니다.

- `projectId`: `projectId`
- `X-Requester-Id`: `ownerId`

목록에 `userId`가 `ownerId`이고 `role`이 `OWNER`인 멤버가 있으면 정상입니다.

### 6단계: 두 번째 사용자를 프로젝트에 추가

`POST /api/projects/{projectId}/members`를 실행합니다.

- `projectId`: `projectId`
- `X-Requester-Id`: `ownerId`

```json
{
  "userId": 2,
  "role": "MEMBER"
}
```

`userId`의 `2`는 예시이므로 실제 `memberId`로 바꿉니다. 역할에는 `OWNER`, `ADMIN`, `MEMBER` 중 하나를 사용할 수 있습니다.

정상 응답 코드는 `201`입니다. 이후 멤버 목록을 다시 조회하면 OWNER와 MEMBER 두 명이 보여야 합니다.

### 7단계: 작업 생성

Swagger의 **Tasks**에서 `POST /api/projects/{projectId}/tasks`를 실행합니다.

- `projectId`: `projectId`
- `X-Requester-Id`: `ownerId`

```json
{
  "title": "REST API 구현",
  "description": "사용자, 프로젝트, 작업 API를 구현한다",
  "status": "TODO",
  "assigneeId": 2
}
```

`assigneeId`의 `2`는 실제 `memberId`로 바꿉니다. 작업 상태에는 `TODO`, `IN_PROGRESS`, `DONE`을 사용할 수 있습니다.

정상 응답 코드는 `201`입니다. 응답의 `id`를 `taskId`로 기록합니다.

### 8단계: 작업 목록과 상세 조회

먼저 `GET /api/projects/{projectId}/tasks`를 실행합니다.

- `projectId`: `projectId`
- `X-Requester-Id`: `ownerId`

방금 만든 작업이 목록에 보이면 정상입니다.

이어서 `GET /api/projects/{projectId}/tasks/{taskId}`를 실행합니다.

- `projectId`: `projectId`
- `taskId`: `taskId`
- `X-Requester-Id`: `ownerId`

### 9단계: 작업 수정

`PUT /api/projects/{projectId}/tasks/{taskId}`를 실행합니다.

- `projectId`: `projectId`
- `taskId`: `taskId`
- `X-Requester-Id`: `memberId`

```json
{
  "title": "REST API 구현",
  "description": "기본 CRUD 구현을 완료한다",
  "status": "IN_PROGRESS",
  "assigneeId": 2
}
```

`assigneeId`는 실제 `memberId`로 바꿉니다. 이 API는 `PUT`이므로 제목과 상태를 포함한 전체 수정 내용을 보내야 합니다.

응답의 `status`가 `IN_PROGRESS`로 바뀌었는지 확인합니다.

### 10단계: 내 프로젝트 목록 조회

Swagger의 **Projects**에서 `GET /api/projects`를 실행합니다.

- `X-Requester-Id`: `ownerId`

생성한 프로젝트가 목록에 나오면 정상입니다.

## 4. 추가로 시험할 API

### 멤버 역할을 ADMIN으로 변경

`PATCH /api/projects/{projectId}/members/{userId}`를 실행합니다.

- `projectId`: `projectId`
- `userId`: `memberId`
- `X-Requester-Id`: `ownerId`

```json
{
  "role": "ADMIN"
}
```

### 프로젝트 수정

`PUT /api/projects/{projectId}`를 실행합니다.

- `projectId`: `projectId`
- `X-Requester-Id`: `ownerId`

```json
{
  "name": "협업 서비스 개발 완료",
  "description": "프로젝트 설명 수정 테스트"
}
```

### 삭제 API

삭제는 다른 실습을 모두 마친 뒤 다음 순서로 실행하는 것이 좋습니다.

1. `DELETE /api/projects/{projectId}/tasks/{taskId}` — 작업 삭제
2. `DELETE /api/projects/{projectId}/members/{userId}` — MEMBER 삭제
3. `DELETE /api/projects/{projectId}` — 프로젝트 삭제

삭제 성공 응답은 본문이 없는 `204 No Content`입니다.

## 5. 자주 발생하는 문제

### `X-Requester-Id` 입력란이 비어 있음

사용자 API를 제외한 프로젝트, 멤버, 작업 API에는 대부분 `X-Requester-Id`가 필요합니다. 현재 요청을 수행하는 사용자의 ID를 입력합니다.

### `404 Not Found`

서버 재실행으로 H2 데이터가 초기화됐거나 잘못된 `userId`, `projectId`, `taskId`를 입력했을 가능성이 큽니다. 사용자 생성부터 다시 진행하고 응답의 실제 ID를 사용합니다.

### `409 Conflict`

이미 사용 중인 이메일로 사용자를 만들었거나, 같은 사용자를 프로젝트에 두 번 추가했을 가능성이 큽니다.

### `400 Bad Request`

필수 필드가 비어 있거나 이메일·상태·역할 값의 형식이 잘못된 경우입니다. 상태와 역할은 아래 대문자 값만 사용합니다.

- 상태: `TODO`, `IN_PROGRESS`, `DONE`
- 역할: `OWNER`, `ADMIN`, `MEMBER`

## 6. H2 Console 접속 정보

H2 Console 로그인 화면에는 다음 값을 입력합니다.

| 항목 | 값 |
| :-- | :-- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:projectcollab;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` |
| User Name | `sa` |
| Password | 비워 둠 |

기본값인 `jdbc:h2:mem:testdb`를 사용하면 현재 애플리케이션 데이터베이스에 연결되지 않습니다.

## 7. 현재 구현 단계 참고

이 가이드는 현재 2단계의 기본 CRUD 흐름을 확인하기 위한 것입니다. 권한 규칙의 완전한 적용, 작업 검색·상태 필터·페이징, 동시 수정 제어는 다음 구현 단계에서 추가 검증해야 합니다.
