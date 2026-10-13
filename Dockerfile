# 1단계: 빌드 (JDK로 jar 파일 생성)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
# 윈도우에서 커밋된 경우 실행 권한이 빠질 수 있어 명시적으로 부여
RUN chmod +x ./gradlew && ./gradlew bootJar --no-daemon

# 2단계: 실행 (JRE만 담아 이미지 크기를 줄임)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
