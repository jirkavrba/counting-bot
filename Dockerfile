FROM gradle:7.4.2-jdk17 AS build

RUN mkdir /app
WORKDIR /app

COPY ./build.gradle /app/build.gradle
COPY ./settings.gradle /app/settings.gradle

RUN gradle clean build --info --no-daemon > /dev/null 2>&1 || true

COPY ./src  /app/src

RUN gradle bootJar --info --no-daemon


FROM openjdk:17-slim AS runtime

RUN mkdir /app
WORKDIR /app

COPY --from=build /app/build/libs/counting-bot*.jar /app/bot.jar

ENTRYPOINT ["java", "-jar", "/app/bot.jar"]