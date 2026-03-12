############################################
# 1단계: 빌드 전용 스테이지
############################################
FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN sed -i 's/\r$//' gradlew && chmod +x gradlew

RUN ./gradlew clean bootJar -x test


############################################
# 2단계: 실행 전용 스테이지
############################################
FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java","-jar","/app/app.jar"]
