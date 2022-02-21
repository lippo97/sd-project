FROM gradle:jdk11 as BUILD

COPY . /buildDir
WORKDIR /buildDir

RUN gradle --no-daemon examples:lpaas-service:distTar

FROM openjdk:11-jre

COPY --from=BUILD /buildDir/examples/lpaas-service/build/distributions/lpaas-service-0.1.tar /app.tar
RUN ["tar", "-xf", "app.tar"]
WORKDIR /lpaas-service-0.1
CMD ["bin/lpaas-service"]
