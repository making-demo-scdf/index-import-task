#!/bin/sh

APP=index-import-task
SERVICE=df-mysql

./mvnw clean package -DskipTests=true
cf v3-push $APP -b java_buildpack_offline -p target/index-import-task-0.0.1-SNAPSHOT.jar
cf v3-bind-service $APP $SERVICE