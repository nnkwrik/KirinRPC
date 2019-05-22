#!/usr/bin/env bash

docker stop zookeeper
docker rm zookeeper

docker run --name zookeeper -p 2181:2181  --restart always -d zookeeper


docker stop zkui
docker rm zkui


hostAddr=$(ifconfig docker0 | grep "inet\b" | awk '{print $2}' | cut -d/ -f1)

docker run --name zkui -e ZKLIST="$hostAddr:2181" -p 9090:9090 -d maauso/zkui
