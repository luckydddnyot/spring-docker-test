# =========================================================================
# Cloud Run 연습용 Dockerfile (멀티 스테이지)
# 1단계(build): Maven 이미지에서 jar 빌드
# 2단계(run)  : 가벼운 JRE 이미지에 jar만 복사해서 실행
# => 최종 이미지에 Maven/소스가 안 들어가서 가볍고, 자바도 박스 안에 포함됨.
# =========================================================================

# ---- 1단계: 빌드 ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- 2단계: 실행 ----
FROM eclipse-temurin:17-jre
WORKDIR /app
# 위 빌드 단계에서 만들어진 jar를 복사 (이름이 바뀌어도 되도록 와일드카드 사용)
COPY --from=build /app/target/*.jar app.jar
# 문서용(실제 포트는 PORT 환경변수로 결정됨). Cloud Run 기본 8080.
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
