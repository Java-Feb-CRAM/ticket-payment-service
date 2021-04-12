#!/bin/sh

export ECS_INSTANCE_IP_ADDRESS=$(/usr/bin/curl --retry 5 --connect-timeout 3 -s 'https://api.ipify.org?format=json' | /usr/bin/jq .ip -r)
exec java ${JAVA_OPTS} -Deureka.instance.ip-address=${ECS_INSTANCE_IP_ADDRESS} -jar /app/app.jar "$@"
