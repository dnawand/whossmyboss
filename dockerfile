FROM gradle:jdk11-alpine AS builder

WORKDIR /app
COPY . .
RUN ./gradlew clean build --no-daemon

FROM openjdk:11-slim

WORKDIR /app
COPY --from=builder /app/build/libs/whosmyboss-1.0.0-all.jar .
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "whosmyboss-1.0.0-all.jar" ]