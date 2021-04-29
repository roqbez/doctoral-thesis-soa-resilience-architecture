#!/bin/bash

./docker_hostenter.sh "tcp://127.0.0.1:2375" "find /var/lib/docker/containers -type f -name "*.log" -exec rm -f {} \;"
./docker_hostenter.sh "tcp://150.162.9.15:2375" "find /var/lib/docker/containers -type f -name "*.log" -exec rm -f {} \;"