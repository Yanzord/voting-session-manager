#! /bin/bash -e

./gradlew clean build

docker build -t voting-session-manager:latest .

docker-compose up -d