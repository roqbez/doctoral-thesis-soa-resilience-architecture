#!/bin/bash

DOCKER_OPTS="-H tcp://127.0.0.1:2375" ./recreate-container.sh bpel-exporter
DOCKER_OPTS="-H tcp://127.0.0.1:2375" ./recreate-container.sh catalog-service
DOCKER_OPTS="-H tcp://127.0.0.1:2375" ./recreate-container.sh service-federation

DOCKER_OPTS="-H tcp://150.162.6.131:2376" ./recreate-container.sh deployment-service
DOCKER_OPTS="-H tcp://150.162.6.131:2376" ./recreate-container.sh discovery-service
DOCKER_OPTS="-H tcp://150.162.6.131:2376" ./recreate-container.sh jms-broker
DOCKER_OPTS="-H tcp://150.162.6.131:2376" ./recreate-container.sh service-registry

DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh resilience-service-haproxy
DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh resilience-service
DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh resilience-service2

DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh binding-service-haproxy
DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh binding-service
DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh binding-service2

DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh execution-service-haproxy
DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh execution-service
DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh execution-service2
                                          
DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh ubl-services
DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh ubl-services2
DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh ubl-services3
DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh ubl-services4
DOCKER_OPTS="-H tcp://150.162.6.133:2376" ./recreate-container.sh ubl-services5
