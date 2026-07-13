# Railway 배포 가이드

## 1. GitHub에 push

```bash
git add .
git commit -m "chore: Railway 배포 설정"
git push origin master
```

## 2. Railway 프로젝트 생성

1. [railway.app](https://railway.app) 로그인
2. **New Project** → **Deploy from GitHub repo**
3. `reservation` 저장소 선택
4. Root Directory: `backend/reservation` (모노레포면 해당 경로)

## 3. Railway Variables

DB URL/계정/비밀번호는 `application.yml`에 들어 있습니다.  
Railway에 **추가로 필요한 것**은 Firebase(와 CORS)입니다.

| Variable | 설명 | 예시 |
|----------|------|------|
| `FIREBASE_CREDENTIALS_JSON` | Firebase Admin JSON **전체 내용** (한 줄) | `{"type":"service_account",...}` |
| `CORS_ALLOWED_ORIGINS` | 프론트/WebView 허용 origin (쉼표 구분) | `https://your-app.com,http://localhost:5173` |

> `PORT`는 Railway가 자동 주입 — 설정 불필요

### 중요: DB 관련 변수는 넣지 마세요 (삭제)

이번 로그 오류 원인:

```
Driver ... claims to not accept jdbcUrl, postgresql://neondb_owner:...@...neon.tech/...
```

→ Railway에 `DATABASE_URL` / `SPRING_DATASOURCE_URL` 이 **Neon 복사본(`postgresql://...`)** 으로 들어가 있으면 JDBC가 거절합니다.
스프링은 `jdbc:postgresql://...` 만 받습니다.

Variables에서 **전부 삭제**:

- `DATABASE_URL`
- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

코드가 yml DB 값을 강제로 쓰도록 보정했지만, 변수는 없는 편이 안전합니다.

### FIREBASE_CREDENTIALS_JSON 넣는 방법

1. Firebase Console → 프로젝트 설정 → 서비스 계정
2. **새 비공개 키 생성** → JSON 다운로드
3. JSON 파일 내용 **전체**를 복사
4. Railway Variable `FIREBASE_CREDENTIALS_JSON`에 붙여넣기 (한 줄로)

## 4. 도메인 설정

Railway → Settings → Networking → **Generate Domain**  
예: `https://reservation-production.up.railway.app`

프론트/앱 API base URL을 이 주소로 바꾸세요.

## 5. 로컬 개발

`application.yml`의 DB·Firebase 경로로 바로 실행하면 됩니다.

```bash
./gradlew bootRun
```

## 6. 트러블슈팅

| 증상 | 확인 |
|------|------|
| Firebase 초기화 실패 | `FIREBASE_CREDENTIALS_JSON` 값 확인 |
| CORS 오류 | `CORS_ALLOWED_ORIGINS`에 프론트 origin 추가 |
| DB 연결 실패 | Neon이 살아 있는지, `application.yml` JDBC URL이 `jdbc:postgresql://` 인지 |
