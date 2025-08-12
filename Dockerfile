FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY ./ /app

RUN ./gradlew clean build -x test

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]