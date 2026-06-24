# 평가 시스템 (GCP Cloud Run + Supabase) — 핸드오프 문서

> 다른 에이전트/개발자가 이어받기 위한 인수인계 문서. 회사 운영 코드(`C:\workspace\talentwise\hr`)와 **완전히 분리된 학습/프로토타입 프로젝트**임.

## 1. 개요
- **무엇**: 멀티테넌트 인사/프로젝트 평가 시스템 v1. 슈퍼어드민이 회사별로 평가유형을 켜고, 평가자가 입력/제출, 관리자가 집계를 본다.
- **스택**: Spring Boot 3.3.5 / Java 17 / JPA(Hibernate) + PostgreSQL / Lombok. 정적 HTML+vanilla JS 프론트(단일 `index.html`).
- **인프라**: GCP Cloud Run(컨테이너 서버리스) + Supabase(PostgreSQL) + GitHub Actions(CI/CD) + Secret Manager.

## 2. 핵심 좌표 (식별자)
| 항목 | 값 |
|------|-----|
| 로컬 경로 | `C:\workspace\cloudrun-practice` |
| GitHub repo | `https://github.com/luckydddnyot/spring-docker-test` (Public) |
| 배포 URL | `https://demo-744948527143.asia-northeast3.run.app/` |
| GCP 프로젝트 ID | `spring-docker-test-500406` (프로젝트번호 `744948527143`) |
| 리전 | `asia-northeast3` (서울) |
| Cloud Run 서비스명 | `demo` |
| Supabase 프로젝트 ref | `tjovpanugutyruxdzivi` |
| DB 접속(Session pooler, IPv4) | host `aws-1-ap-northeast-2.pooler.supabase.com` / port `5432` / db `postgres` / user `postgres.tjovpanugutyruxdzivi` |
| DB 비밀번호 | GCP Secret Manager 시크릿 `db-password` (연습용 평문값: `gjwlsdud12387`) |
| 배포용 SA | `github-deployer@spring-docker-test-500406.iam.gserviceaccount.com` |
| GitHub 시크릿 | `GCP_SA_KEY`(배포 SA 키 JSON), `ANTHROPIC_API_KEY`(@claude 액션용) |

> ⚠️ 연습용 SA 키가 대화에 노출된 적 있음 → **운영 전환 시 키 재발급 필수**.

## 3. 배포 파이프라인
- **CI/CD**: `main`에 push → `.github/workflows/deploy.yml` 실행 → `gcloud run deploy demo --source .`(Cloud Build로 Dockerfile 빌드) → Cloud Run 갱신.
  - DB 접속정보 주입: `--set-env-vars`(URL/USERNAME) + `--set-secrets "SPRING_DATASOURCE_PASSWORD=db-password:latest"`.
- **@claude**: `.github/workflows/claude.yml` — 이슈/댓글에 `@claude` 멘션 시 자동 구현/PR. (권한에 `id-token: write` 필수)
- **컨테이너**: `Dockerfile` 멀티스테이지(maven 빌드 → JRE 실행). `application.properties`의 `server.port=${PORT:8080}`가 Cloud Run 필수 포인트.
- **스키마**: `spring.jpa.hibernate.ddl-auto=update`로 테이블 자동생성. `DataSeeder`가 최초 1회 데모데이터 시드.

## 4. 평가 앱 설계
### 계층
```
슈퍼어드민 → Company(회사) → CompanyEvalType(회사별 HR/PROJECT on/off)
   → Evaluatee(피평가자, 회사·유형 소속) → Goal(가중 목표=업적) + CompetencyScore(역량)
   → 평가자: 입력/임시저장/제출 → 관리자: 진행률·등급분포·결과·CSV
```
### 엔티티 (`src/main/java/com/example/demo/`)
- `Company`(id, name)
- `CompanyEvalType`(companyId, evalType[HR|PROJECT], enabled)
- `Evaluatee`(companyId, evalType, name, dept, position, evaluatorName, status[NONE|DRAFT|DONE], overallComment)
- `Goal`(evaluateeId, title, weight%, score 1~5, sortOrder)
- `CompetencyScore`(evaluateeId, itemName, score 1~5)
- `CompetencyTemplate`(evalType, itemName, description, sortOrder) ← 역량 항목 정의(하드코딩 X, 데이터로 관리)

### 점수 계산 (`Scoring.java`) — v1 고정값
- 업적 = 목표 점수의 **가중평균**(weight%), 역량 = 점수 **단순평균**
- **종합 = 업적×0.6 + 역량×0.4**
- 등급: 4.5↑ **S** / 4.0↑ **A** / 3.0↑ **B** / 2.0↑ **C** / 미만 **D**

### API
| 메서드/경로 | 설명 |
|---|---|
| `GET /api/companies` | 회사목록(+사용중 유형) — 회사 셀렉터용 |
| `GET /api/companies/{cid}/targets?evaluator=` | 피평가자 목록(상태·진행률) |
| `GET /api/targets/{id}` | 평가 상세(목표/역량/점수/종합/등급) |
| `PUT /api/targets/{id}` | 저장(임시저장 DRAFT / 제출 DONE). body: `{goals:[{id,score}], competencies:[{itemName,score}], overallComment, status}` |
| `GET /api/companies/{cid}/summary` | 관리자 집계(KPI/평가자별/등급분포/결과) |
| `GET /api/admin/companies` | (슈퍼어드민) 회사+유형 설정 목록 |
| `POST /api/admin/companies` | 회사 등록 `{name}` |
| `PUT /api/admin/companies/{id}/types` | 유형 on/off `{"HR":true,"PROJECT":false}` |
| `GET /health`, `GET /db` | 헬스/DB체크(레거시 VisitLog 기반, 평가와 무관) |

### 프론트 (`src/main/resources/static/index.html`)
- 단일 파일, vanilla JS `fetch`. 헤더에 **회사 셀렉터** + 탭 3개(**평가자/관리자/슈퍼어드민**).
- 평가 폼: 점수 클릭 시 **메모리(cur)만 갱신 후 `renderForm()` 재그리기**, 저장은 임시저장/제출 때만 `PUT`. (※ 초기 버그: 클릭마다 재조회해 값이 되돌아갔음 → 수정 완료)

### 시드 데이터 (`DataSeeder.java`)
- 회사: **A컨설팅**(PROJECT), **B커머스**(HR) + 역량 템플릿(유형별 5개).
- 각 회사 피평가자 3명 + 목표 + 일부 점수(DONE/DRAFT/NONE 섞음).
- ⚠️ 운영 중 슈퍼어드민 테스트로 데이터 변경됨: `Deloitte Consulting` 회사 추가됨, `A컨설팅`이 HR로 토글된 상태일 수 있음(현재 DB 기준). 신규 환경은 시드대로.

## 5. 파일 구조
```
cloudrun-practice/
├─ pom.xml                      # spring-boot-starter-web, data-jpa, postgresql, lombok
├─ Dockerfile                   # 멀티스테이지(maven→JRE)
├─ .github/workflows/
│   ├─ deploy.yml               # push→Cloud Run 자동배포
│   └─ claude.yml               # @claude 이슈 자동화
└─ src/main/
    ├─ java/com/example/demo/
    │   ├─ DemoApplication.java         # 부트스트랩 + /health,/db
    │   ├─ Company/CompanyEvalType/Evaluatee/Goal/CompetencyScore/CompetencyTemplate.java
    │   ├─ Repositories.java            # 모든 JpaRepository 인터페이스
    │   ├─ Scoring.java                 # 점수/등급 계산
    │   ├─ EvaluationController.java     # /api/...
    │   ├─ SuperAdminController.java     # /api/admin/...
    │   ├─ DataSeeder.java              # 데모 시드
    │   └─ VisitLog.java, VisitLogRepository.java   # 레거시(평가 무관, 제거 가능)
    └─ resources/
        ├─ application.properties       # server.port=${PORT:8080}, datasource(env), ddl-auto=update
        └─ static/index.html           # 전체 UI(3탭)
```

## 6. 로컬 빌드/배포
- **빌드**(이 repo엔 maven wrapper 없음 → 회사 repo wrapper로 외부 pom 빌드):
  ```powershell
  Set-Location 'C:\workspace\talentwise\hr'
  $env:JAVA_HOME='C:\ms-17.0.18'; $env:Path="$env:JAVA_HOME\bin;$env:Path"
  .\mvnw.cmd -f 'C:\workspace\cloudrun-practice\pom.xml' -B -ntp -DskipTests package
  ```
  (또는 Dockerfile이 컨테이너 안에서 maven으로 빌드하므로, 배포는 빌드 불필요)
- **배포**: 로컬에서 `git add -A && git commit && git push` → GitHub Actions가 자동 배포. (파일 삭제 반영 위해 `git add -A` 사용)

## 7. 현재 상태
- ✅ Cloud Run 배포·동작, Supabase 저장 정상, CI/CD·@claude·Secret Manager 구성 완료.
- ✅ 점수 버튼 버그 수정 커밋 (push 후 하드 새로고침 Ctrl+Shift+R 필요 — 정적파일 브라우저 캐시 주의).

## 8. 미해결 / 다음 작업 (Phase C 후보)
- 로그인/권한(현재 역할·회사를 화면에서 선택, 인증 없음)
- 목표(업적) **CSV 업로드** / 관리자 목표 등록 화면
- 가중치(60/40)·등급기준·역량항목 **회사별 관리자 설정**(현재 코드 고정 → 설정테이블화)
- 미제출 **알림**(이메일 등)
- 레거시 `VisitLog`/`/db` 정리, 커스텀 도메인 매핑, 운영용 SA 키 재발급
- (선택) 멀티테넌트 보안: 현재 API에 회사 격리 강제 없음 → tenant 컨텍스트/인증 도입 시 강화 필요
