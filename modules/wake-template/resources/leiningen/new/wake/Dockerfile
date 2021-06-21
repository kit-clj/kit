# syntax = docker/dockerfile:1.2
FROM clojure:openjdk-11-lein AS build

WORKDIR /
COPY . /

RUN lein uberjar

FROM adoptopenjdk/openjdk11:alpine-slim

COPY --from=build /target/uberjar/{{app}}.jar /{{app}}/{{app}}.jar

EXPOSE $PORT
EXPOSE $REPL_PORT

ENTRYPOINT exec java $JAVA_OPTS -jar /{{app}}/{{app}}.jar

