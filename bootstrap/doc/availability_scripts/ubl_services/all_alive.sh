#!/bin/bash

function call_service {
	printf "\n"
	URL=$1
	OP=$2
	PAYLOAD="<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:int=\"http://interfaces.ubl.specifications.services.gsigma.ufsc.br/\"><soapenv:Header/><soapenv:Body><int:${OP}/></soapenv:Body></soapenv:Envelope>"
	echo "$URL ($OP)"
	curl -s -H "Content-Type: text/xml;charset=UTF-8" -X POST -d ''"$PAYLOAD"'' "$URL" | xmllint --format --xpath '//return' -
	printf "\n"
}


call_service "http://ublservices.d-201603244.ufsc.br:11000/services/UBLServicesAdminService" "setServiceAvailable"
call_service "http://ublservices2.d-201603244.ufsc.br:11001/services/UBLServicesAdminService" "setServiceAvailable"
call_service "http://ublservices3.d-201603244.ufsc.br:11002/services/UBLServicesAdminService" "setServiceAvailable"
call_service "http://ublservices4.d-201603244.ufsc.br:11003/services/UBLServicesAdminService" "setServiceAvailable"
call_service "http://ublservices5.d-201603244.ufsc.br:11004/services/UBLServicesAdminService" "setServiceAvailable"


