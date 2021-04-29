#!/bin/bash

es2csv -u logging.d-201603244.ufsc.br:9200 \
-d "," \
-q '(message_id:RESILIENCE_REACTION AND reconfigurationDone:true) OR (message_id:PROCESS_INSTANCE AND processFinished:true AND processStateCode:(30 70))' \
-o result.csv \
-f \
@timestamp \
message_id \
processInstanceId \
processCreationTime \
processFinishTime \
processDuration \
numberOfBindingReconfigurations \
QoS.Constraints.Performance.ResponseTime \
resilienceReactionMeanTime \
resilienceDiscoveryMeanTime \
resilienceDeploymentMeanTime \
resilienceServicesCheckMeanTime \