# 1. OpenJDK 이미지를 베이스 이미지로 사용
FROM openjdk:21-jdk-slim as build

# 2. Selenium 설정을 위해 필요한 패키지 설치
RUN apt-get update && apt-get install -y wget gnupg2 && \
    wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list && \
    apt-get update && \
    apt-get install -y google-chrome-stable

# 3. 작업 디렉토리 설정
WORKDIR /app

# 4. 소스 코드와 Gradle 스크립트를 컨테이너로 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src

# 5. 애플리케이션 빌드
RUN ./gradlew build -x test

# 6. 빌드가 완료된 후 실행을 위한 새로운 스테이지
FROM openjdk:21-jdk-slim

# 7. 작업 디렉토리 설정
WORKDIR /app

# 8. 빌드 스테이지에서 생성된 JAR 파일을 복사
COPY --from=build /app/build/libs/*.jar app.jar

# 9. 빌드 스테이지에서 resource 파일들을 최종 이미지로 복사
COPY --from=build /app/src/main/resources ./src/main/resources

# 10. 런타임에 필요한 패키지 설치
RUN apt-get update && apt-get install -y wget gnupg2 && \
    wget -q -O - https://dl.google.com/linux/linux_signing_key.pub | apt-key add - && \
    echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list && \
    apt-get update && \
    apt-get install -y google-chrome-stable

# 11. 런타임에 파일이 생성될 경로에 대한 볼륨 설정
VOLUME ["/app/emails", "/app/base", "/app/wallet"]

# 12. 애플리케이션 실행
ENTRYPOINT ["java","-jar","/app/app.jar"]