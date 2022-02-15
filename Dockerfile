FROM openjdk:11-jdk as BUILD

COPY . /buildDir
WORKDIR /buildDir
RUN ./gradlew --no-daemon main-vertx-mongo:distTar

FROM openjdk:11-jre

COPY --from=BUILD /buildDir/main-vertx-mongo/build/distributions/main-vertx-mongo-0.1.tar /app.tar
RUN ["tar", "-xf", "app.tar"]
WORKDIR /main-vertx-mongo-0.1
CMD ["bin/main-vertx-mongo"]
