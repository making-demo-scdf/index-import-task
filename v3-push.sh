#!/bin/sh

APP=index-import-task
SERVICE=df-mysql

./mvnw clean package -DskipTests=true
java -uvf target/index-import-task-1.0.0-dev1.jar Procfile
cf v3-push $APP -b java_buildpack_offline -p target/index-import-task-1.0.0-dev1.jar
cf v3-bind-service $APP $SERVICE