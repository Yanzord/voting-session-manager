#! /bin/bash -e

app_container_id=$(docker ps --filter name=voting-session-manager --quiet)
mongo_container_id=$(docker ps --filter name=mongo --quiet)

app_image_id=$(docker image ls --filter reference=voting-session-manager --quiet)
mongo_image_id=$(docker image ls --filter reference=mongo --quiet)

docker stop $app_container_id && docker rm $app_container_id
docker stop $mongo_container_id && docker rm $mongo_container_id

docker rmi -f $app_image_id
docker rmi -f $mongo_image_id