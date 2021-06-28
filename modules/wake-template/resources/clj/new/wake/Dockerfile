# syntax = docker/dockerfile:1.2
FROM clojure:openjdk-11 AS build

WORKDIR /
COPY . /

RUN clojure -X:uberjar

FROM adoptopenjdk/openjdk11:alpine-slim

COPY --from=build /target/uberjar/<<name>>.jar /<<name>>/<<name>>.jar

EXPOSE $PORT
EXPOSE $REPL_PORT

ENTRYPOINT exec java $JAVA_OPTS -jar /<<name>>/<<name>>.jar

