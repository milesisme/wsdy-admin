#!/bin/bash
cd `dirname $0`/..
tag=`git status|grep 'On branch'|awk '{print $3}'`
docker build -f docker/Dockerfile.saasops-v2 -t saasops-v2:$tag .



