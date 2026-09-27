# Board REST API

Spring Boot 3.5.16, Java 17, Spring Security, Spring Data JPA, and file-based H2를 사용하는 모놀리식 게시판 API입니다. 이 디렉터리는 기존 `board` 프로젝트와 독립되어 있습니다.

## 실행

- JDK 17 이상
- 별도 DB 설치 없이 H2 사용
- 프로젝트 디렉터리에서 `./gradlew bootRun` 실행 (Windows는 `gradlew.bat bootRun`)
- 서버: `http://localhost:8080`
- H2 파일 DB: `./data/board`; 콘솔 `http://localhost:8080/h2-console` (JDBC URL은 YAML 값 참고)

Spring Boot 3.x 최신 패치 버전인 3.5.16을 사용했습니다. [공식 릴리스 공지](https://spring.io/blog/2026/06/25/spring-boot-3-5-16-available-now/)

## 인증 및 설계

- 인증은 서버 세션 방식입니다. 브라우저/일반 HTTP 클라이언트에서 사용할 수 있고 서버가 세션을 무효화해 로그아웃을 처리합니다. 로그인 성공 시 `JSESSIONID`를 HttpOnly 쿠키로 발급합니다.
- REST API 중심으로 JSON 로그인과 명시적인 세션 저장을 사용합니다. CSRF 검사는 비활성화되어 있으므로 브라우저에서 운영할 때는 CSRF 토큰을 추가하거나 동일 출처 정책을 적용해야 합니다.
- 회원은 게시글과 댓글에 각각 1:N 관계입니다. 게시글과 댓글은 회원을 다대일로 참조합니다. 게시글 삭제 시 댓글도 cascade/orphan removal로 함께 삭제됩니다.
- 게시글 목록은 프로젝션 집계 쿼리 한 번과 페이지 전체 건수 쿼리로 닉네임·댓글 수를 반환합니다. 목록에서 엔티티 연관관계를 반복 조회하지 않아 N+1이 발생하지 않습니다. 댓글 목록은 작성자를 `@EntityGraph`로 함께 읽습니다.
- 모든 API는 DTO만 요청/응답으로 사용합니다. 오류는 `timestamp`, `status`, `code`, `message`, `path` 필드를 가진 동일한 JSON 형태입니다.

## API 명세

| 메서드 | 주소 | 인증 | 요청 | 성공 응답 |
|---|---|---:|---|---|
| POST | `/api/auth/signup` | 불필요 | `{"email":"a@example.com","password":"password123","nickname":"Alice"}` | 201 회원 DTO (비밀번호 제외) |
| POST | `/api/auth/login` | 불필요 | `{"email":"a@example.com","password":"password123"}` | 200 회원 식별 정보, `Set-Cookie: JSESSIONID` |
| POST | `/api/auth/logout` | 필요 | 없음 | 204, 세션 무효화 |
| POST | `/api/posts` | 필요 | `{"title":"제목","content":"본문"}` | 201 게시글 DTO |
| GET | `/api/posts?page=0&size=10` | 불필요 | 없음 | 200 페이지, 최신순 |
| GET | `/api/posts/{postId}` | 불필요 | 없음 | 200 게시글 DTO |
| PUT | `/api/posts/{postId}` | 필요, 작성자만 | 게시글 요청 DTO | 200 수정된 게시글 |
| DELETE | `/api/posts/{postId}` | 필요, 작성자만 | 없음 | 204 |
| POST | `/api/posts/{postId}/comments` | 필요 | `{"content":"댓글"}` | 201 댓글 DTO |
| GET | `/api/posts/{postId}/comments` | 불필요 | 없음 | 200 댓글 배열 |
| PATCH | `/api/comments/{commentId}` | 필요, 작성자만 | `{"content":"수정 댓글"}` | 200 댓글 DTO |
| DELETE | `/api/comments/{commentId}` | 필요, 작성자만 | 없음 | 204 |

가입 중복 이메일은 409, 잘못된 입력은 400, 로그인 실패/미인증은 401, 타인 리소스 변경은 403, 없는 게시글·댓글은 404입니다. 페이지 `size`는 1~100입니다.

오류 응답 예:

```json
{"timestamp":"2026-09-28T01:02:03Z","status":401,"code":"UNAUTHORIZED","message":"로그인이 필요합니다.","path":"/api/posts"}
```

## 실행 예시

아래는 서버에서 실행할 수 있는 순서별 curl 예시입니다. 로그인 응답의 세션 쿠키는 `-c`로 저장하고 다음 호출에 `-b`로 보냅니다.

```bash
curl -i -X POST http://localhost:8080/api/auth/signup -H 'Content-Type: application/json' -d '{"email":"alice@example.com","password":"password123","nickname":"Alice"}'
# 201 {"id":1,"email":"alice@example.com","nickname":"Alice","createdAt":"2026-09-28T10:00:00"}

curl -i -c cookies.txt -X POST http://localhost:8080/api/auth/login -H 'Content-Type: application/json' -d '{"email":"alice@example.com","password":"password123"}'
# 200 {"memberId":1,"email":"alice@example.com","nickname":"Alice","message":"로그인되었습니다."}
# Set-Cookie: JSESSIONID=...; HttpOnly; SameSite=Lax

curl -i -b cookies.txt -X POST http://localhost:8080/api/posts -H 'Content-Type: application/json' -d '{"title":"첫 글","content":"게시판 시작"}'
# 201 {"id":1,"title":"첫 글","content":"게시판 시작","authorId":1,"authorNickname":"Alice",...}

curl -i -b cookies.txt -X POST http://localhost:8080/api/posts/1/comments -H 'Content-Type: application/json' -d '{"content":"첫 댓글"}'
# 201 {"id":1,"postId":1,"authorId":1,"authorNickname":"Alice","content":"첫 댓글",...}

curl -i 'http://localhost:8080/api/posts?page=0&size=10'
# 200 {"content":[{"id":1,"title":"첫 글","authorNickname":"Alice","commentCount":1,...}],"page":0,"size":10,...}
```

401 확인(로그인 없이 글 작성):

```bash
curl -i -X POST http://localhost:8080/api/posts -H 'Content-Type: application/json' -d '{"title":"실패","content":"인증 필요"}'
# 401 {"timestamp":"...","status":401,"code":"UNAUTHORIZED","message":"로그인이 필요합니다.","path":"/api/posts"}
```

403 확인: 다른 회원을 가입·로그인시킨 뒤 그 회원의 `cookies.txt`로 Alice의 글을 수정합니다.

```bash
curl -i -b other-cookies.txt -X PUT http://localhost:8080/api/posts/1 -H 'Content-Type: application/json' -d '{"title":"변경 시도","content":"권한 없음"}'
# 403 {"timestamp":"...","status":403,"code":"FORBIDDEN","message":"요청을 수행할 권한이 없습니다.","path":"/api/posts/1"}
```
