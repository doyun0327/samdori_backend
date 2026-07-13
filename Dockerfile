# ---- build ----
# Gradle이 미리 설치된 이미지 사용 → wrapper가 HTTPS로 zip 받을 필요 없음
FROM gradle:8.14.3-jdk17-jammy AS build
WORKDIR /app

COPY build.gradle settings.gradle gradle.properties ./
COPY src src

RUN gradle bootJar -x test --no-daemon

# ---- run ----
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN groupadd -r spring && useradd -r -g spring spring
USER spring:spring

COPY --from=build /app/build/libs/*.jar app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
