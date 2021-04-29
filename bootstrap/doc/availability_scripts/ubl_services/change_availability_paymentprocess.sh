#!/bin/bash

function call_service {
	printf "\n"
	URL=$1

	PAYLOAD='
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:int="http://interfaces.ubl.specifications.services.gsigma.ufsc.br/">
   <soapenv:Header/>
   <soapenv:Body>
      <int:setRandomServiceAvailabilityCombination>
         <servicePathNumberOfServices>
            <entry key="/services/ubl/paymentprocess/accountingCustomer/authorizePayment" value="1"/>
            <entry key="/services/ubl/paymentprocess/accountingCustomer/notifyOfPayment" value="1"/>
            <entry key="/services/ubl/paymentprocess/accountingSupplier/notifyPayee" value="1"/>
            <entry key="/services/ubl/paymentprocess/accountingSupplier/receiveAdvice" value="1"/>
            <entry key="/services/ubl/paymentprocess/payeeParty/receiveAdvice" value="1"/>
         </servicePathNumberOfServices>
         <host>http://ublservices.d-201603244.ufsc.br:11000</host>
         <host>http://ublservices2.d-201603244.ufsc.br:11001</host>
         <host>http://ublservices3.d-201603244.ufsc.br:11002</host>
         <host>http://ublservices4.d-201603244.ufsc.br:11003</host>
         <host>http://ublservices5.d-201603244.ufsc.br:11004</host>
      </int:setRandomServiceAvailabilityCombination>
   </soapenv:Body>
</soapenv:Envelope>
'
	
	echo "$URL"
	curl -s -H "Content-Type: text/xml;charset=UTF-8" -X POST -d ''"$PAYLOAD"'' "$URL" | xmllint --xpath "//*[local-name()='setRandomServiceAvailabilityCombinationResponse']" -
	printf "\n"
}


while true 
do

call_service "http://ublservices.d-201603244.ufsc.br:11000/services/UBLServicesAdminService" "setRandomServiceAvailability" "accountingCustomer/authorizePayment"

sleep 2

done

