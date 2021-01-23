#! /bin/bash -e

./gradlew build

docker build -t voting-session-manager:latest .

docker-compose up -d