FROM gradle:8.7-jdk21 AS build

WORKDIR /app

COPY . .

RUN gradle shadowJar --no-daemon

FROM eclipse-temurin:21-alpine

WORKDIR /app

COPY --from=build /app/build/libs/*.jar /app.jar

CMD ["java", "-jar", "/app.jar"]
