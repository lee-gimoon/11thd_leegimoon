# Swagger API 실습 가이드

이 문서는 현재 구현된 프로젝트 협업 서비스 API를 Swagger UI에서 순서대로 실행하기 위한 안내서입니다.

이 실습에서는 사용자를 **총 2명 생성**합니다.

- 사용자 1: 프로젝트를 만들고 관리하는 `OWNER`
- 사용자 2: 프로젝트에 참여하고 작업을 담당하는 `MEMBER`

`POST /api/users`를 한 번만 실행하는 것이 아니라, 서로 다른 이름과 이메일로 **두 번 실행해야 합니다.** 이후 응답에서 받은 두 사용자 ID를 각각 다른 용도로 사용합니다.

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

아래 값은 뒤의 모든 API에서 반복해서 사용하므로 반드시 메모합니다. 표의 예시는 서버를 새로 실행한 직후 순서대로 생성했을 때 흔히 나오는 값이며, Swagger의 **실제 Server response 값이 우선**입니다.

| 이름 | 의미 | 예상 예시 | 실제 응답에서 기록할 값 |
| :-- | :-- | :-- | :-- |
| `ownerId` | 첫 번째로 생성한 OWNER 사용자 ID | `1` | |
| `memberId` | 두 번째로 생성한 MEMBER 사용자 ID | `2` | |
| `projectId` | OWNER가 생성한 프로젝트 ID | `1` | |
| `taskId` | 프로젝트에 생성한 작업 ID | `1` | |

`X-Requester-Id`는 현재 요청을 수행하는 사용자의 ID입니다. 인증 기능 대신 이 헤더로 요청자를 전달합니다.

## 3. 전체 실습 순서

아래 단계는 서로 이어지는 하나의 시나리오입니다. 중간 단계를 건너뛰지 말고 1단계부터 순서대로 실행합니다.

### 1단계: OWNER와 MEMBER 사용자 2명 생성

사용자 2명 생성은 하나의 필수 준비 단계입니다. **Users**의 `POST /api/users`를 서로 다른 정보로 두 번 실행합니다.

#### 1-1. OWNER로 사용할 첫 번째 사용자 생성

Swagger의 **Users**에서 `POST /api/users`를 실행합니다.

```json
{
  "name": "이기문",
  "email": "owner@example.com"
}
```

정상 응답 코드는 `201 Created`입니다. Swagger 위쪽의 **Server response → Response body**에 있는 실제 `id`를 `ownerId`로 기록합니다. 아래쪽 **Responses → Example Value**에 표시되는 `id: 0`은 문서 예시이므로 사용하지 않습니다.

```json
{
  "id": 1,
  "name": "이기문",
  "email": "owner@example.com"
}
```

위의 `id: 1`은 예시입니다. 실제 응답에 나온 값을 사용해야 합니다.

```text
ownerId = ______
```

#### 1-2. MEMBER로 사용할 두 번째 사용자 생성

같은 `POST /api/users`를 **두 번째로 다시 실행합니다.** 첫 번째 사용자와 별도로 저장되는 두 번째 사용자입니다.

```json
{
  "name": "김멤버",
  "email": "member@example.com"
}
```

정상 응답 코드는 `201 Created`입니다. 실제 응답은 다음과 같은 형태입니다.

```json
{
  "id": 2,
  "name": "김멤버",
  "email": "member@example.com"
}
```

응답의 실제 `id`를 `memberId`로 기록합니다. 이메일은 중복될 수 없으므로 첫 번째 사용자와 다른 이메일을 입력해야 합니다.

```text
memberId = ______
```

이 시점에 H2 데이터베이스에는 다음 두 사용자가 있어야 합니다.

| 구분 | 이름 | 이메일 | 이후 용도 |
| :-- | :-- | :-- | :-- |
| 사용자 1 | 이기문 | `owner@example.com` | 프로젝트 생성자 및 OWNER |
| 사용자 2 | 김멤버 | `member@example.com` | 프로젝트에 추가할 MEMBER 및 작업 담당자 |

두 응답에서 `ownerId`와 `memberId`를 모두 기록해야 1단계가 완료됩니다.

### 2단계: 사용자 조회 확인

`GET /api/users/{userId}`를 총 두 번 실행해 두 사용자를 모두 확인합니다.

- 첫 번째 실행의 `userId`: `ownerId`
- 두 번째 실행의 `userId`: `memberId`

각 실행에서 응답 코드 `200 OK`와 해당 사용자 정보가 나오면 정상입니다.

### 3단계: 프로젝트 생성

Swagger의 **Projects**에서 `POST /api/projects`를 실행합니다.

- `X-Requester-Id`: `ownerId`

```json
{
  "name": "협업 서비스 개발",
  "description": "React와 Spring Boot를 사용하는 채용 과제"
}
```

정상 응답 코드는 `201`입니다. 응답의 `id`를 `projectId`로 기록합니다.

```text
projectId = ______
```

프로젝트를 생성한 사용자는 해당 프로젝트의 `OWNER` 멤버로 자동 등록됩니다.

프로젝트 생성 직후 **Projects**의 `GET /api/projects/{projectId}`도 실행합니다.

- `projectId`: 방금 기록한 `projectId`
- `X-Requester-Id`: `ownerId`

응답 코드 `200 OK`와 생성한 프로젝트의 이름·설명이 나오면 정상입니다.

### 4단계: OWNER 자동 등록 확인

Swagger의 **Project Members**에서 `GET /api/projects/{projectId}/members`를 실행합니다.

- `projectId`: `projectId`
- `X-Requester-Id`: `ownerId`

목록에 `userId`가 `ownerId`이고 `role`이 `OWNER`인 멤버가 있으면 정상입니다.

### 5단계: 두 번째 사용자를 프로젝트에 추가

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

두 멤버의 의미를 구분해서 확인합니다.

- OWNER 행: `userId`가 `ownerId`, `role`이 `OWNER`
- MEMBER 행: `userId`가 `memberId`, `role`이 `MEMBER`

### 6단계: 작업 생성

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

```text
taskId = ______
```

### 7단계: 작업 목록과 상세 조회

먼저 `GET /api/projects/{projectId}/tasks`를 실행합니다.

- `projectId`: `projectId`
- `X-Requester-Id`: `ownerId`
- `keyword`: 비워 둠 — 입력하면 제목과 설명에서 검색
- `status`: 비워 둠 — `TODO`, `IN_PROGRESS`, `DONE` 중 하나로 필터 가능
- `page`: `0` — 첫 페이지는 0부터 시작
- `size`: `20` — 한 페이지의 작업 수, 최대 100

작업 목록은 배열 자체가 아니라 페이지 정보와 함께 반환됩니다. 방금 만든 작업이 `content` 안에 보이면 정상입니다.

```json
{
  "content": [
    {
      "id": 1,
      "projectId": 1,
      "title": "REST API 구현",
      "status": "TODO",
      "assigneeId": 2,
      "assigneeName": "김멤버",
      "version": 0
    }
  ],
  "page": 0,
  "size": 20,
  "totalElements": 1,
  "totalPages": 1,
  "first": true,
  "last": true,
  "hasNext": false,
  "hasPrevious": false
}
```

추가로 다음 조건도 시험할 수 있습니다.

- `keyword=API`: 제목 또는 설명에 API가 포함된 작업 검색
- `status=TODO`: TODO 상태 작업만 조회
- `keyword=API`, `status=TODO`: 두 조건을 모두 만족하는 작업 조회
- `page=1`, `size=2`: 두 번째 페이지를 두 건씩 조회

검색 결과는 현재 `projectId`의 작업만 포함하며 최신 생성순으로 정렬됩니다.

이어서 `GET /api/projects/{projectId}/tasks/{taskId}`를 실행합니다.

- `projectId`: `projectId`
- `taskId`: `taskId`
- `X-Requester-Id`: `ownerId`

응답의 `version`을 기록합니다. 작업 생성 직후라면 보통 `0`이지만 반드시 실제 응답 값을 사용합니다.

```text
현재 작업 version = ______
```

### 8단계: 작업 수정

`PUT /api/projects/{projectId}/tasks/{taskId}`를 실행합니다.

- `projectId`: `projectId`
- `taskId`: `taskId`
- `X-Requester-Id`: `memberId`

```json
{
  "title": "REST API 구현",
  "description": "기본 CRUD 구현을 완료한다",
  "status": "IN_PROGRESS",
  "assigneeId": 2,
  "version": 0
}
```

`assigneeId`는 실제 `memberId`, `version`은 바로 앞에서 조회한 실제 버전으로 바꿉니다. 이 API는 `PUT`이므로 제목과 상태를 포함한 전체 수정 내용을 보내야 합니다.

응답의 `status`가 `IN_PROGRESS`로 바뀌고 `version`이 이전 값보다 1 증가했는지 확인합니다.

#### 오래된 버전 충돌 확인

방금 성공한 수정 요청을 다시 실행하되 `version`은 증가하기 전의 오래된 값인 `0`을 그대로 사용합니다. 그러면 두 번째 요청은 저장되지 않고 다음과 같이 `409 Conflict`가 반환되어야 합니다.

```json
{
  "code": "TASK_VERSION_CONFLICT",
  "message": "다른 사용자가 작업을 먼저 수정했습니다. 최신 내용을 다시 조회해 주세요.",
  "status": 409
}
```

다시 수정하려면 `GET /api/projects/{projectId}/tasks/{taskId}`로 최신 작업을 조회하고, 새 `version`을 수정 요청에 넣어야 합니다.

### 9단계: 내 프로젝트 목록 조회

Swagger의 **Projects**에서 `GET /api/projects`를 실행합니다.

- 첫 번째 실행의 `X-Requester-Id`: `ownerId`
- 두 번째 실행의 `X-Requester-Id`: `memberId`

OWNER와 MEMBER 모두 자신이 참여한 동일한 프로젝트를 목록에서 확인할 수 있어야 합니다.

## 4. 이어서 모든 수정·삭제 API 확인

여기부터도 선택 사항이 아닙니다. 현재 구현된 모든 API 흐름을 확인하려면 순서대로 계속 실행합니다.

### 10단계: MEMBER 역할을 ADMIN으로 변경

`PATCH /api/projects/{projectId}/members/{userId}`를 실행합니다.

- `projectId`: `projectId`
- `userId`: `memberId`
- `X-Requester-Id`: `ownerId`

```json
{
  "role": "ADMIN"
}
```

응답 코드 `200 OK`와 `role: "ADMIN"`을 확인합니다. 이어서 멤버 목록을 다시 조회해 두 번째 사용자의 역할도 `ADMIN`으로 바뀌었는지 확인합니다.

### 11단계: 프로젝트 수정하고 다시 조회

`PUT /api/projects/{projectId}`를 실행합니다.

- `projectId`: `projectId`
- `X-Requester-Id`: `ownerId`

```json
{
  "name": "협업 서비스 개발 완료",
  "description": "프로젝트 설명 수정 테스트"
}
```

응답 코드 `200 OK`와 수정된 이름을 확인합니다. 이어서 `GET /api/projects/{projectId}`를 다시 실행해 변경 내용이 저장됐는지 확인합니다.

- `projectId`: `projectId`
- `X-Requester-Id`: `ownerId`

### 12단계: 애플리케이션 상태 API 확인

Swagger의 **System**에서 `GET /api/health`를 실행합니다. 이 API에는 ID나 Request body가 필요하지 않습니다.

```json
{
  "status": "UP",
  "application": "project-collab"
}
```

응답 코드 `200 OK`와 `status: "UP"`이 나오면 서버가 정상입니다.

### 13단계: 작업 삭제하고 삭제 결과 확인

다른 작업 실습을 모두 마친 뒤 `DELETE /api/projects/{projectId}/tasks/{taskId}`를 실행합니다.

- `projectId`: `projectId`
- `taskId`: `taskId`
- `X-Requester-Id`: `memberId`

성공 응답은 본문이 없는 `204 No Content`입니다.

삭제 후 `GET /api/projects/{projectId}/tasks/{taskId}`를 같은 ID로 다시 실행합니다.

- 예상 결과: `404 Not Found`
- 의미: 삭제된 작업은 더 이상 조회되지 않음

### 14단계: 두 번째 프로젝트 멤버 제거

`DELETE /api/projects/{projectId}/members/{userId}`를 실행합니다.

- `projectId`: `projectId`
- `userId`: `memberId`
- `X-Requester-Id`: `ownerId`

성공 응답은 본문이 없는 `204 No Content`입니다.

삭제 후 `GET /api/projects/{projectId}/members`를 실행합니다.

- `projectId`: `projectId`
- `X-Requester-Id`: `ownerId`
- 예상 결과: OWNER 한 명만 남은 목록

여기서 삭제되는 것은 사용자가 아니라 **프로젝트 참여 관계인 ProjectMember**입니다. 사용자 2의 User 데이터는 그대로 남아 있습니다.

### 15단계: 프로젝트 삭제

마지막으로 `DELETE /api/projects/{projectId}`를 실행합니다.

- `projectId`: `projectId`
- `X-Requester-Id`: `ownerId`

프로젝트 삭제는 OWNER가 수행합니다. 성공 응답은 본문이 없는 `204 No Content`입니다.

삭제 후 `GET /api/projects/{projectId}`를 같은 ID로 다시 실행합니다.

- `projectId`: 삭제한 `projectId`
- `X-Requester-Id`: `ownerId`
- 예상 결과: `404 Not Found`

이어서 `GET /api/projects`를 `ownerId`로 실행했을 때 삭제한 프로젝트가 목록에 없어야 합니다.

### 16단계: 사용자 데이터가 남아 있는지 확인

프로젝트를 삭제해도 사용자 자체가 삭제되는 것은 아닙니다. **Users**에서 `GET /api/users/{userId}`를 두 번 실행합니다.

- 첫 번째 `userId`: `ownerId`
- 두 번째 `userId`: `memberId`

두 사용자 모두 `200 OK`로 조회되면 전체 실습이 끝난 것입니다.

## 5. 전체 API 실행 체크리스트

완료한 항목에 체크합니다.

- [ ] `POST /api/users` — OWNER 사용자 생성
- [ ] `POST /api/users` — MEMBER 사용자 생성
- [ ] `GET /api/users/{userId}` — 두 사용자 조회
- [ ] `POST /api/projects` — 프로젝트 생성
- [ ] `GET /api/projects` — OWNER와 MEMBER의 참여 프로젝트 목록 조회
- [ ] `GET /api/projects/{projectId}` — 프로젝트 상세 조회
- [ ] `PUT /api/projects/{projectId}` — 프로젝트 수정
- [ ] `GET /api/projects/{projectId}/members` — OWNER 자동 등록과 멤버 목록 확인
- [ ] `POST /api/projects/{projectId}/members` — 두 번째 사용자 추가
- [ ] `PATCH /api/projects/{projectId}/members/{userId}` — 역할 변경
- [ ] `POST /api/projects/{projectId}/tasks` — MEMBER가 담당자인 작업 생성
- [ ] `GET /api/projects/{projectId}/tasks` — 작업 목록 조회
- [ ] `GET /api/projects/{projectId}/tasks/{taskId}` — 작업 상세 조회
- [ ] `PUT /api/projects/{projectId}/tasks/{taskId}` — 담당자가 작업 수정
- [ ] `GET /api/health` — 서버 상태 확인
- [ ] `DELETE /api/projects/{projectId}/tasks/{taskId}` — 작업 삭제
- [ ] `DELETE /api/projects/{projectId}/members/{userId}` — 두 번째 멤버 제거
- [ ] `DELETE /api/projects/{projectId}` — 프로젝트 삭제
- [ ] 삭제 후 작업과 프로젝트의 `404` 응답 확인
- [ ] 프로젝트 삭제 후에도 두 사용자 조회 가능 여부 확인

## 6. 자주 발생하는 문제

### `X-Requester-Id` 입력란이 비어 있음

사용자 API를 제외한 프로젝트, 멤버, 작업 API에는 대부분 `X-Requester-Id`가 필요합니다. 현재 요청을 수행하는 사용자의 ID를 입력합니다.

### `404 Not Found`

서버 재실행으로 H2 데이터가 초기화됐거나 잘못된 `userId`, `projectId`, `taskId`를 입력했을 가능성이 큽니다. 사용자 생성부터 다시 진행하고 응답의 실제 ID를 사용합니다.

### `409 Conflict`

이미 사용 중인 이메일로 사용자를 만들었거나, 같은 사용자를 프로젝트에 두 번 추가했을 가능성이 큽니다.

마지막 OWNER의 역할을 변경하거나 프로젝트에서 제거하려 해도 `LAST_OWNER_REQUIRED` 코드와 함께 `409`가 반환됩니다. 프로젝트에는 항상 최소 한 명의 OWNER가 있어야 합니다.

작업을 조회했을 때보다 DB 버전이 먼저 증가한 경우에는 `TASK_VERSION_CONFLICT`가 반환됩니다. 최신 작업을 다시 조회한 뒤 새 버전으로 수정해야 합니다.

### `403 Forbidden`

`X-Requester-Id`의 사용자가 프로젝트에 속하지 않았거나 요청한 기능을 수행할 역할이 없는 경우입니다. 응답의 `code`로 원인을 구분합니다.

- `PROJECT_ACCESS_DENIED`: 프로젝트 비멤버
- `PROJECT_PERMISSION_DENIED`: 프로젝트 멤버이지만 OWNER·ADMIN 등 필요한 역할이 없음
- `TASK_PERMISSION_DENIED`: 작업 담당자 또는 OWNER·ADMIN이 아니어서 수정·삭제할 수 없음

### `400 Bad Request`

필수 필드가 비어 있거나 이메일·상태·역할 값의 형식이 잘못된 경우입니다. 상태와 역할은 아래 대문자 값만 사용합니다.

- 상태: `TODO`, `IN_PROGRESS`, `DONE`
- 역할: `OWNER`, `ADMIN`, `MEMBER`

작업 목록에서 `page`가 음수이거나 `size`가 1 미만 또는 100 초과이면 `INVALID_PAGINATION` 코드가 반환됩니다.

## 7. H2 Console 접속 정보

H2 Console 로그인 화면에는 다음 값을 입력합니다.

| 항목 | 값 |
| :-- | :-- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:projectcollab;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE` |
| User Name | `sa` |
| Password | 비워 둠 |

기본값인 `jdbc:h2:mem:testdb`를 사용하면 현재 애플리케이션 데이터베이스에 연결되지 않습니다.

## 8. 현재 구현 및 검증 범위

기본 CRUD, 프로젝트 역할 권한, 작업 검색·필터·페이징과 동시 수정 제어를 구현하고 자동 테스트와 실제 HTTP 실행 검증까지 완료했습니다.

- 프로젝트 비멤버의 모든 프로젝트·멤버·작업 접근 차단
- 프로젝트 수정은 OWNER·ADMIN, 삭제는 OWNER만 허용
- 멤버 관리는 OWNER·ADMIN만 허용
- 프로젝트에 최소 한 명의 OWNER 유지
- 작업 생성·조회는 프로젝트 멤버 전원에게 허용
- 작업 수정·삭제는 담당자 또는 OWNER·ADMIN만 허용
- 작업 담당자는 같은 프로젝트의 멤버만 지정 가능
- 담당 중인 멤버를 제거하면 해당 프로젝트 작업의 담당자는 미할당으로 변경
- 작업 제목·설명 키워드 검색과 상태 필터 지원
- 0부터 시작하는 페이지 조회, 최대 페이지 크기 100
- 최신 생성순과 ID 역순을 함께 사용해 안정적인 정렬 보장
- Task의 JPA `@Version`과 수정 요청의 버전 값으로 오래된 변경 차단
- 실제 동시 수정 또는 오래된 버전 요청에 `409 TASK_VERSION_CONFLICT` 반환

동시성 처리의 SQL 동작 원리는 [작업 동시성 문제 해결 방법](./작업_동시성_문제_해결_방법.md)에서 확인할 수 있습니다.
