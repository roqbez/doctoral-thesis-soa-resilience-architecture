#!/bin/bash

DOCKER_OPTS="-H tcp://150.162.6.131:2376" ../../../docker-utils/docker-backup-volume.sh catalog-service-data
DOCKER_OPTS="-H tcp://150.162.6.131:2376" ../../../docker-utils/docker-backup-volume.sh discovery-service-data
DOCKER_OPTS="-H tcp://150.162.6.131:2376" ../../../docker-utils/docker-backup-volume.sh service-federation-data
DOCKER_OPTS="-H tcp://150.162.6.131:2376" ../../../docker-utils/docker-backup-volume.sh service-registry_data


