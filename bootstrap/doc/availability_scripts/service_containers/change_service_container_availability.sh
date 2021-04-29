#!/bin/bash

function call_service {
	printf "\n"
	URL=$1

	PAYLOAD='
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:int="http://interfaces.deployment.services.gsigma.ufsc.br/">
   <soapenv:Header/>
   <soapenv:Body>
      <int:removeAllServiceContainers>
         <deploymentServer>SERVER_150_162_6_131</deploymentServer>
      </int:removeAllServiceContainers>
   </soapenv:Body>
</soapenv:Envelope>
'
	
	echo "$URL"
	curl -s -H "Content-Type: text/xml;charset=UTF-8" -X POST -d ''"$PAYLOAD"'' "$URL" | xmllint --xpath "//*[local-name()='removeAllServiceContainersResponse']" -
	printf "\n"
}


while true 
do

call_service "http://deploymentservice.d-201603244.ufsc.br:6000/services/DeploymentService"

sleep 60

done

