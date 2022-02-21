FROM gradle:jdk11 as BUILD

COPY . /buildDir
WORKDIR /buildDir

RUN gradle --no-daemon examples:authentication-service:distTar

FROM openjdk:11-jre

COPY --from=BUILD /buildDir/examples/authentication-service/build/distributions/authentication-service-0.1.tar /app.tar
RUN ["tar", "-xf", "app.tar"]
WORKDIR /authentication-service-0.1
CMD ["bin/authentication-service"]
