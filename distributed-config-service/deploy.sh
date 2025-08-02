#!/bin/bash

VERSION=1.0.0
IMAGE=distributed-config-service
NEXUS=localhost:8081/docker-hosted/zengcode

docker build -t $NEXUS/$IMAGE:$VERSION ./distributed-config-service
docker push $NEXUS/$IMAGE:$VERSION