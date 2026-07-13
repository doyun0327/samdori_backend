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

## 3. Railway Variables (필수)

Railway 대시보드 → Service → **Variables** 탭

| Variable | 설명 | 예시 |
|----------|------|------|
| `SPRING_DATASOURCE_URL` | Neon JDBC URL | `jdbc:postgresql://ep-xxx.neon.tech/neondb?sslmode=require` |
| `SPRING_DATASOURCE_USERNAME` | Neon 사용자 | `neondb_owner` |
| `SPRING_DATASOURCE_PASSWORD` | Neon 비밀번호 | `(Neon에서 복사)` |
| `FIREBASE_CREDENTIALS_JSON` | Firebase Admin JSON **전체 내용** (한 줄) | `{"type":"service_account",...}` |
| `CORS_ALLOWED_ORIGINS` | 프론트/WebView 허용 origin (쉼표 구분) | `https://your-app.com,http://localhost:5173` |
| `SHOW_SQL` | SQL 로그 (운영: false) | `false` |

> `PORT`는 Railway가 자동 주입 — 설정 불필요

### FIREBASE_CREDENTIALS_JSON 넣는 방법

1. Firebase Console → 프로젝트 설정 → 서비스 계정
2. **새 비공개 키 생성** → JSON 다운로드
3. JSON 파일 내용 **전체**를 복사
4. Railway Variable `FIREBASE_CREDENTIALS_JSON`에 붙여넣기 (한 줄로)

## 4. 도메인 설정

1. Service → **Settings** → **Networking**
2. **Generate Domain** 클릭
3. `https://xxx.up.railway.app` 발급됨

## 5. Flutter / React에 API URL 변경

```
https://xxx.up.railway.app
```

예:
- `POST https://xxx.up.railway.app/api/user/login`
- `POST https://xxx.up.railway.app/api/devices/token`

## 6. 배포 확인

```bash
curl https://xxx.up.railway.app/api/user/counselors
```

Railway **Deployments** 탭에서 로그 확인:
- `Firebase Admin SDK 초기화 완료`
- `Started ReservationApplication`

## 7. Neon DB

Railway DB를 쓰지 않고 **기존 Neon** 그대로 사용합니다.
Neon SQL Editor에서 `sql/` 폴더 스크립트가 모두 실행됐는지 확인하세요.

## 트러블슈팅

| 증상 | 확인 |
|------|------|
| Build 실패 (SSL / NoSuchAlgorithmException) | `gradle.properties`에 `Windows-ROOT` / Windows JDK 경로가 없는지 확인 |
| Build 실패 | Railway 로그에서 Gradle 오류 확인 |
| DB 연결 실패 | `SPRING_DATASOURCE_*` 3개 값 확인 |
| FCM 안 됨 | `FIREBASE_CREDENTIALS_JSON` JSON 형식 확인 |
| CORS 오류 | `CORS_ALLOWED_ORIGINS`에 프론트 URL 추가 |
| 502 / crash | Deployments 로그에서 startup exception 확인 |

### 로컬 Windows JDK 경로

로컬에서 JDK 경로를 고정하려면 **프로젝트 `gradle.properties`가 아니라**  
사용자 홈의 `C:\Users\<이름>\.gradle\gradle.properties`에 넣으세요.

```properties
org.gradle.java.home=C:\\Program Files\\Java\\jdk-17
```
